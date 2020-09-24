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

data class TestElementList(
    val testsWithCases: List<PsiElement>,
    val testMissingCases: List<PsiElement>,
) {
    companion object {
        fun empty() = TestElementList(emptyList(), emptyList())
    }
}

interface ValetFile {
    /**
     * Used to pass the element this class shall work with. Return false if the element is not supported by this
     * implementation Or if it should for any other reason not be considered for a gutter marker.
     */
    fun accept(element: PsiElement): Boolean

    fun isTest(): Boolean
    fun isSource(): Boolean
    fun getSources(): List<PsiElement>
    fun getTests(): TestElementList
    fun markerTarget(): PsiElement
}
