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
import com.intellij.psi.PsiElement
import lafreakshow.plugins.valet.gutter.NavigationIconBuilder
import lafreakshow.plugins.valet.things.ValetSourceElement

class JavaGutterMarkerProvider : GutterMarkerProvider() {
    override fun isSupportedElement(element: PsiElement): Boolean {
        TODO("Not yet implemented")
    }

    override fun isTestClass(element: PsiElement): Boolean {
        TODO("Not yet implemented")
    }

    override fun isSourceClass(element: PsiElement): Boolean {
        TODO("Not yet implemented")
    }

    override fun getValetSourceElement(element: PsiElement): ValetSourceElement {
        TODO("Not yet implemented")
    }

    override fun getValetTestElement(element: PsiElement): ValetSourceElement {
        TODO("Not yet implemented")
    }
}
