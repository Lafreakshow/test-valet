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

package lafreakshow.plugins.valet.gutter.provider

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import lafreakshow.plugins.valet.things.ValetSourceElement
import lafreakshow.plugins.valet.things.ValetTestElement
import lafreakshow.plugins.valet.util.logger

enum class ElementSupport {
    TEST_CLASS, SOURCE_CLASS, UNSUPPORTED
}

internal typealias ResultList = MutableCollection<in RelatedItemLineMarkerInfo<*>>

abstract class GutterMarkerProvider : RelatedItemLineMarkerProvider() {
    protected open val log: Logger by logger()

    final override fun collectNavigationMarkers(element: PsiElement, result: ResultList) {
        when (determineSupport(element)) {
            ElementSupport.SOURCE_CLASS -> {
                log.debug("'$element' is a Source Class")
                val sourceElement = getValetSourceElement(element)
                val iconBuilder = sourceElement.icon.forTarget(element)
                sourceElement.getRelatedElements().forEach {
                    iconBuilder.
                }
            }
            ElementSupport.TEST_CLASS   -> {
                log.debug("'$element' is a Test Class")
            }
            ElementSupport.UNSUPPORTED  -> {
                log.debug("'$element' is unsupported")
            }
        }
    }

    abstract fun determineSupport(element: PsiElement): ElementSupport

    abstract fun getValetSourceElement(element: PsiElement): ValetSourceElement
    abstract fun getValetTestElement(element: PsiElement): ValetTestElement
}
