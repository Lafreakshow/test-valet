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

package lafreakshow.plugins.valet.things

import com.intellij.psi.PsiElement
import lafreakshow.plugins.valet.gutter.NavigationIconBuilder

abstract class ValetElement(val element: PsiElement) {
    abstract val icon: NavigationIconBuilder
    abstract fun getRelatedElements(): List<PsiElement>
}

abstract class ValetTestElement(element: PsiElement) : ValetElement(element) {
    /**
     * Set True if known source elements exist. Source Elements will be taken from [getRelatedElements]
     */
    abstract val hasKnownSources: Boolean
}

abstract class ValetSourceElement(element: PsiElement): ValetElement(element) {
    /**
     * Set True if at least one known test case exists. Source Elements will be taken from [getRelatedElements]
     */
    abstract val hasKnownTestCases: Boolean

    /**
     * Set True if known Test elements exist. Elements will be taken from [getRelatedElements]
     */
    abstract val hasKnownTestClass: Boolean
}
