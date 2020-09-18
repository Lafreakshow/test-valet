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

package lafreakshow.plugins.valet.gutter

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import javax.swing.Icon

object NavigationIcons {
    val testClassFound: Icon by lazy { IconLoader.getIcon("/icons/greenTest.png") }
    val testClassMissing: Icon by lazy { IconLoader.getIcon("/icons/greenTest.png") }
    val testClassWithoutCases: Icon by lazy { IconLoader.getIcon("/icons/yellowTest.png") }

    val sourceClassFound: Icon by lazy { IconLoader.getIcon("/icons/greenSource.png") }
    val sourceClassMissing: Icon by lazy { IconLoader.getIcon("/icons/redSource.png") }
}

data class NavigationIconBuilder(
    val icon: Icon,
    val tooltipText: String
) {
    companion object {
        val TEST_CLASS_FOUND = NavigationIconBuilder(NavigationIcons.testClassFound, "Go to Test")
        val TEST_CLASS_MISSING = NavigationIconBuilder(NavigationIcons.testClassMissing, "No Test class found")
        val TEST_CLASS_WITHOUT_CASES = NavigationIconBuilder(
            NavigationIcons.testClassWithoutCases,
            "No Test cases found in Test Class"
        )

        val SOURCE_CLASS_FOUND = NavigationIconBuilder(NavigationIcons.sourceClassFound, "Go to Source")
        val SOURCE_CLASS_MISSING = NavigationIconBuilder(NavigationIcons.sourceClassMissing, "No Source class found")
    }

    fun forTargets(targets: List<PsiElement>): NavigationGutterIconBuilder<PsiElement> =
        NavigationGutterIconBuilder.create(icon)
            .setTooltipText(tooltipText)
            .setTargets(targets)
}
