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
import com.intellij.psi.PsiElement
import lafreakshow.plugins.valet.api.GutterMarkerProvider
import lafreakshow.plugins.valet.api.TestElementList
import lafreakshow.plugins.valet.api.ValetFile
import lafreakshow.plugins.valet.settings.ValetSettings
import lafreakshow.plugins.valet.util.JvmLanguageUtil.findJvmElementsNamed
import lafreakshow.plugins.valet.util.JvmLanguageUtil.hasSuffixes
import lafreakshow.plugins.valet.util.JvmLanguageUtil.splitIntoTestElementList
import lafreakshow.plugins.valet.util.logger
import lafreakshow.plugins.valet.util.removeFirstMatchingSuffix
import lafreakshow.plugins.valet.util.withSuffixVariants
import org.jetbrains.kotlin.psi.KtClassOrObject

class KotlinGutterMarkerProvider : GutterMarkerProvider(KotlinValetFile::class)

// TODO: Somehow support top level functions, probably reasonable to just look for test classes named after the file
class KotlinValetFile : ValetFile {
    private val log: Logger by logger()
    private val testSuffixes: ArrayList<String> by ValetSettings.settingsDelegator()

    // There's also KtClass and KtObjectDeclaration to use when they need to be differentiated
    private lateinit var clazz: KtClassOrObject

    override fun accept(element: PsiElement): Boolean =
        if (element is KtClassOrObject && element.nameIdentifier != null) {
            clazz = element; true
        } else {
            false
        }

    override fun isTest(): Boolean = hasSuffixes(clazz, testSuffixes)

    override fun isSource(): Boolean = !isTest()

    override fun getSources(): List<PsiElement> =
        findJvmElementsNamed(clazz, clazz.fqName!!.asString().removeFirstMatchingSuffix(testSuffixes))

    override fun getTests(): TestElementList = splitIntoTestElementList(
        clazz,
        clazz.fqName!!.asString().withSuffixVariants(testSuffixes)
    )

    override fun markerTarget(): PsiElement = clazz.nameIdentifier ?: clazz
}
