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

package lafreakshow.plugins.valet.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.panel

class ValetSettingsConfigurable : BoundConfigurable("Valet Test") {
    val settings = ValetSettings.getInstance().settingsSstate

    val suffixBinding = PropertyBinding<String>(
        { settings.testSuffixes.joinToString(", ") },
        { settings.testSuffixes = ArrayList(it.split(", ").map { it.trim() }) }
    )

    override fun createPanel() = panel {
        titledRow("Basic Settings") {
            row {
                label("Test Suffixes")
                textField(suffixBinding).comment(
                    "Comma separated list of test file/class suffixes. surrounding whitespace is ignored.",
                    90,
                    forComponent = true
                )
            }
        }
    }
}
