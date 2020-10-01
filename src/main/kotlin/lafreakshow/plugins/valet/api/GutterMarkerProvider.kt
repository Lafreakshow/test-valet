/*
 *    Copyright 2020 Lafreakshow
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package lafreakshow.plugins.valet.api

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import lafreakshow.plugins.valet.api.NavigationIconBuilder.Companion.SOURCE_CLASS_FOUND
import lafreakshow.plugins.valet.api.NavigationIconBuilder.Companion.SOURCE_CLASS_MISSING
import lafreakshow.plugins.valet.api.NavigationIconBuilder.Companion.TEST_CLASS_FOUND
import lafreakshow.plugins.valet.api.NavigationIconBuilder.Companion.TEST_CLASS_MISSING
import lafreakshow.plugins.valet.api.NavigationIconBuilder.Companion.TEST_CLASS_WITHOUT_CASES
import lafreakshow.plugins.valet.util.computePsiElementString
import lafreakshow.plugins.valet.util.debug
import lafreakshow.plugins.valet.util.logger
import lafreakshow.plugins.valet.util.trace
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

internal typealias ResultList = MutableCollection<in RelatedItemLineMarkerInfo<*>>

/**
 * Base class adding support for lafreakshow.plugins.valet.languages.
 *
 * The java implementation is the reference implementation so for an example take a look at
 * JavaGutterMarkerProvider.kt in the lafreakshow.plugins.valet.languages package.
 *
 * Create a subclass of [GutterMarkerProvider] and pass a [ValetFile] subclass to the super constructor. Then
 * register your subclass as an extension with IntelliJ. Most of the functionality should be implemented in a
 * [ValetFile] subclass. It is perfectly possible for [GutterMarkerProvider] subclasses to not even have a body.
 */
// TODO: Split collectNavigationMarkers into multiple functions for easier maintainability,
abstract class GutterMarkerProvider(private val valetFileClass: KClass<out ValetFile>) :
    RelatedItemLineMarkerProvider() {
    protected open val log: Logger by logger()

    final override fun collectNavigationMarkers(element: PsiElement, result: ResultList) {
        val valetFile = valetFileClass.createInstance()

        if (valetFile.accept(element)) {
            log.debug { "${valetFileClass.simpleName} Accepts ${computePsiElementString(element)}" }

            if (valetFile.isTest()) {
                log.trace { "${computePsiElementString(element)} is a test" }

                val sources = valetFile.getSources()
                log.trace { "Sources: ${sources.joinToString(", ", transform = ::computePsiElementString)}" }

                if (sources.isEmpty()) {
                    val markerBuilder = SOURCE_CLASS_MISSING.forTarget(element)
                    result.add(markerBuilder.createLineMarkerInfo(valetFile.markerTarget()))
                } else {
                    val markerBuilder = SOURCE_CLASS_FOUND.forTargets(sources)
                    result.add(markerBuilder.createLineMarkerInfo(valetFile.markerTarget()))
                }
            } else if (valetFile.isSource()) {
                log.trace { "${computePsiElementString(element)} is a source" }

                val (withCases, noCases) = valetFile.getTests()
                log.trace { "Tests (case)   :  ${withCases.joinToString(", ", transform = ::computePsiElementString)}" }
                log.trace { "Tests (no case):  ${noCases.joinToString(", ", transform = ::computePsiElementString)}" }

                if (withCases.isEmpty() && noCases.isEmpty()) {
                    val markerBuilder = TEST_CLASS_MISSING.forTarget(element)
                    result.add(markerBuilder.createLineMarkerInfo(valetFile.markerTarget()))
                } else {
                    // Theoretically there could be two markers one each for tests with and without cases. I don't
                    // think this will ever happen though. But just like I think there won't often be two source
                    // files for single test, the option is there should it ever happen.
                    if (withCases.isNotEmpty()) {
                        val markerBuilder = TEST_CLASS_FOUND.forTargets(withCases)
                        result.add(markerBuilder.createLineMarkerInfo(valetFile.markerTarget()))
                    }

                    if (noCases.isNotEmpty()) {
                        val markerBuilder = TEST_CLASS_WITHOUT_CASES.forTargets(noCases)
                        result.add(markerBuilder.createLineMarkerInfo(valetFile.markerTarget()))
                    }
                }
            }
        }
    }
}
