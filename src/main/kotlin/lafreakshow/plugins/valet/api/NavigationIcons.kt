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

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import javax.swing.Icon

/**
 * Properties contain the raw icon used by the Plugins. If you want to add gutter icon support for other languages, use
 * [NavigationIconBuilder] instead.
 */
object NavigationIcons {
    val testClassFound: Icon by lazy { IconLoader.getIcon("/icons/testExists.svg") }
    val testClassMissing: Icon by lazy { IconLoader.getIcon("/icons/testMissing.svg") }
    val testClassWithoutCases: Icon by lazy { IconLoader.getIcon("/icons/testNoCase.svg") }

    val sourceClassFound: Icon by lazy { IconLoader.getIcon("/icons/sourceExists.svg") }
    val sourceClassMissing: Icon by lazy { IconLoader.getIcon("/icons/sourceMissing.svg") }
}

/**
 * Convenience class to make creating a NavigationIconBuilder less painful. Contains pre configured builders for the
 * five regular icons. Prefer using those over doing anything custom.
 */
data class NavigationIconBuilder(
    val icon: Icon,
    val tooltipText: String
) {

    /**
     * Creates NavigationGutterIconBuilder with the given elements as navigation targets.
     */
    fun forTargets(targets: List<PsiElement>): NavigationGutterIconBuilder<PsiElement> =
        NavigationGutterIconBuilder.create(icon)
            .setTooltipText(tooltipText)
            .setTargets(targets)

    /**
     * like [forTargets] but takes only one element.
     */
    fun forTarget(target: PsiElement): NavigationGutterIconBuilder<PsiElement> =
        NavigationGutterIconBuilder.create(icon)
            .setTooltipText(tooltipText)
            .setTarget(target)

    companion object {
        /** Used when a test class was found and contains test cases.*/
        val TEST_CLASS_FOUND: NavigationIconBuilder =
            NavigationIconBuilder(NavigationIcons.testClassFound, "Go to Test")

        /** Used when no test class was found. */
        val TEST_CLASS_MISSING: NavigationIconBuilder =
            NavigationIconBuilder(NavigationIcons.testClassMissing, "No Test class found")

        /** Used when a test class was found but does not contain test cases. */
        val TEST_CLASS_WITHOUT_CASES: NavigationIconBuilder =
            NavigationIconBuilder(NavigationIcons.testClassWithoutCases, "No Test cases found in Test Class")

        /** Used when a source file was found for a test. */
        val SOURCE_CLASS_FOUND: NavigationIconBuilder =
            NavigationIconBuilder(NavigationIcons.sourceClassFound, "Go to Source")

        /** Used when no source file was found for a test. */
        val SOURCE_CLASS_MISSING: NavigationIconBuilder =
            NavigationIconBuilder(NavigationIcons.sourceClassMissing, "No Source class found")
    }
}
