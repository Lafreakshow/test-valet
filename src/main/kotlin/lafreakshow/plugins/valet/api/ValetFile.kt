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

import com.intellij.psi.PsiElement

/** Encapsulates the return value of [ValetFile.getTests]. */
data class TestElementList(
    val testsWithCases: List<PsiElement>,
    val testMissingCases: List<PsiElement>,
) {
    companion object {
        fun empty() = TestElementList(emptyList(), emptyList())
    }
}

/**
 * Interface providing all the functionality needed to add Test navigation gutter icon for a language. Used by
 * [GutterMarkerProvider], see its documentation for details.
 */
interface ValetFile {
    /**
     * Used to pass the element this class shall work with. Return false if the element is not supported by this
     * implementation or if it should for any other reason not be considered for a gutter marker.
     *
     * This is guaranteed to be the first method called by [GutterMarkerProvider] during the lifetime of an instance
     * implementing this interface.
     */
    fun accept(element: PsiElement): Boolean

    /**
     * Return true if the element passed to [accept] represents a test class. False otherwise. Both [isTest] and
     * [isSource] may be called on the same instance in any order.
     */
    fun isTest(): Boolean

    /**
     * Return true if the element passed to [accept] represents a source class. False otherwise. Both [isTest] and
     * [isSource] may be called on the same instance in any order.
     */
    fun isSource(): Boolean

    /**
     * Return a list of one zero, one or more PsiElements that are sources to this test. Is guaranteed to only be
     * called after [isTest] returns true.
     */
    fun getSources(): List<PsiElement>

    /**
     * Return a [TestElementList], which take two listseach containing tero, one or more PsiElements representing.
     * tests for this source where [TestElementList.testMissingCases] contains tests that do not contain test cases
     * and [TestElementList.testsWithCases] contains tests that do contain test cases.
     *
     * Is guaranteed to only be called after [isSource] returns true.
     */
    fun getTests(): TestElementList

    /**
     * It is recommended to add line markers only on leaf elements. For example, if the element is a Java class, the
     * marker should be added to its PsiIdentifier. Us this method to return th element the marker should be attached
     * to. If there is no specific element, return the element originally passed to [accept].
     */
    fun markerTarget(): PsiElement
}
