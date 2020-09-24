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

package lafreakshow.plugins.valet.languages

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import lafreakshow.plugins.valet.api.GutterMarkerProvider
import lafreakshow.plugins.valet.api.TestElementList
import lafreakshow.plugins.valet.api.ValetFile
import lafreakshow.plugins.valet.settings.ValetSettings
import lafreakshow.plugins.valet.util.getModuleSearchScope
import lafreakshow.plugins.valet.util.logger
import lafreakshow.plugins.valet.util.trace
import java.util.*

class JavaGutterMarkerProvider : GutterMarkerProvider(JavaValetFile::class)

class JavaValetFile : ValetFile {
    private val log: Logger by logger()
    private val testSuffixes: ArrayList<String> by ValetSettings.settingsDelegator()

    lateinit var clazz: PsiClass

    override fun accept(element: PsiElement): Boolean =
        // Anonymous java classes  will have qualifiedName be null. We can't do anything with them
        if (element is PsiClass && element.qualifiedName != null && element.nameIdentifier != null) {
            clazz = element; true
        } else false

    override fun markerTarget(): PsiElement = clazz.nameIdentifier ?: clazz

    override fun isSource(): Boolean = !isTest()

    override fun isTest(): Boolean =
        // If name is null checking for a suffix will always fail so we can shortcut to false
        clazz.name?.let { className -> testSuffixes.any { className.endsWith(it) } } ?: false

    override fun getSources(): List<PsiElement> {
        // JavaValetFile shouldn't accept Anonymous classes, so it shouldn't be possible for qualifiedName to
        // be null at this point. If it does end up being null, we have much larger issues and the stacktrace
        // hopefully leads one to this comment.
        val namesToSearch = testSuffixes.mapNotNull {
            if (clazz.qualifiedName!!.endsWith(it)) {
                clazz.qualifiedName!!.removeSuffix(it)
            } else null
        }

        return findNamedClasses(namesToSearch)
    }

    override fun getTests(): TestElementList {
        // See getSource for why qualifiedName can't be null
        val namesToSearch = testSuffixes.map { clazz.qualifiedName + it }
        val foundClasses = findNamedClasses(namesToSearch)
        val (withCases, noCases) = foundClasses.partition { hasTestCase(it) }

        return if (foundClasses.isEmpty()) {
            TestElementList.empty()
        } else {
            TestElementList(withCases, noCases)
        }
    }

    private fun findNamedClasses(namesToSearch: List<String>): List<PsiClass> {
        val optionalSearchScope = getModuleSearchScope(clazz)
        val psiFacade = JavaPsiFacade.getInstance(clazz.project)

        return if (optionalSearchScope.isPresent) {
            val scope = optionalSearchScope.get()

            log.trace { "Searching for: ${namesToSearch.joinToString(", ")}" }
            log.trace { "Scope: $scope" }

            namesToSearch.mapNotNull { psiFacade.findClass(it, scope) }
        } else emptyList()
    }
}

private val TEST_ANNOTATION_NAMES = listOf(
    "org.junit.Test", // JUnit 4

    // JUnit 5
    "org.junit.jupiter.api.Test",
    "org.junit.jupiter.api.RepeatedTest",
    "org.junit.jupiter.params.ParameterizedTest",
    "org.junit.jupiter.api.TestFactory",
    "org.junit.jupiter.api.TestTemplate",
)

fun hasTestCase(psiClass: PsiClass): Boolean =
    psiClass.methods.any { method -> TEST_ANNOTATION_NAMES.any { method.hasAnnotation(it) } }
