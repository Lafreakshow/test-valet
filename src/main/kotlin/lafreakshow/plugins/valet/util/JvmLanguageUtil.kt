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

package lafreakshow.plugins.valet.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScope
import lafreakshow.plugins.valet.api.TestElementList
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction

// I wanted to keep the API language agnostic for the not all that impossible case that I ever want to add support
// for Python or Javascript based tests but JVM languages like Java, Kotlin and Groovy share a lot both in how they
// work and in how the IntelliJ platform allows you to interact with them. They also tend to offer enough
// interoperability that is conceivable to write tests for a Java based application ins Kotlin or Groovy. Especially
// this latter case requires some crossover between the code to support the languages so it makes sense to pull
// common functionality and code into a common library.
//
// This is what you're looking at. This file contains Functionality shared between the JVM languages like Kotlin,
// Java and Groovy.

@Suppress("MemberVisibilityCanBePrivate")
internal object JvmLanguageUtil {
    private val log: Logger by logger()

    internal val TEST_ANNOTATION_NAMES = listOf(
        "org.junit.Test", // JUnit 4

        // JUnit 5
        "org.junit.jupiter.api.Test",
        "org.junit.jupiter.api.RepeatedTest",
        "org.junit.jupiter.params.ParameterizedTest",
        "org.junit.jupiter.api.TestFactory",
        "org.junit.jupiter.api.TestTemplate",
    )

    /**
     *  Attempts to extract a name from the given element and tests it against the given list of suffixes.
     *
     *  Returns true if the extracted name ends with any of the given suffixes.
     *
     *  Returns false if no suffix matches or a name cannot be extracted.
     */
    internal fun hasSuffixes(element: PsiNamedElement, suffixes: List<String>): Boolean {
        // Technically, a nameless element doe not have a suffix.
        val nameToTestAgainst = element.name ?: return false

        log.trace { "nameToTestAgainst: $nameToTestAgainst" }

        return suffixes.any { nameToTestAgainst.endsWith(it) }
    }

    internal fun findJvmElementsNamed(element: PsiElement, names: Iterable<String>): List<PsiElement> {
        val scope = getModuleSearchScope(element)
        return if (scope.isPresent) {
            findJvmElementsNamed(scope.get(), names)
        } else emptyList()
    }

    // TOD: rewrite using flatMap
    internal fun findJvmElementsNamed(scope: GlobalSearchScope, names: Iterable<String>): List<PsiElement> =
        names.flatMap { findJvmElementsNamed(scope, it) }

    internal fun findJvmElementsNamed(element: PsiElement, name: String): List<PsiElement> {
        val scope = getModuleSearchScope(element)
        return if (scope.isPresent) {
            findJvmElementsNamed(scope.get(), name)
        } else emptyList()
    }

    internal fun findJvmElementsNamed(scope: GlobalSearchScope, name: String): List<PsiElement> {
        val results = mutableListOf<PsiElement>()
        val javaFacade = JavaPsiFacade.getInstance(scope.project)

        // Will Find Java, Kotlin and probably Groovy classes
        results.addAll(javaFacade.findClasses(name, scope))

        log.trace { "Classes for '$name': ${results.joinToString(", ", transform = ::computePsiElementString)}" }
        return results
    }

    internal fun splitIntoTestElementList(element: PsiElement, names: Iterable<String>): TestElementList =
        splitIntoTestElementList(findJvmElementsNamed(element, names))

    internal fun splitIntoTestElementList(elements: Iterable<PsiElement>): TestElementList {
        val (withCase, noCase) = elements.partition(::hasTestCase)
        return TestElementList(withCase, noCase)
    }

    internal fun hasTestCase(element: PsiElement): Boolean = when (element) {
        is PsiClass -> element.methods.any { method ->
            TEST_ANNOTATION_NAMES.any { method.hasAnnotation(it) }
        }
        is KtClass -> element.declarations.any {
            it is KtFunction && it.annotations.any { annotation ->
                TEST_ANNOTATION_NAMES.any { annotation.name == it }
            }
        }
        else        -> false
    }
}
