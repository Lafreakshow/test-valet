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

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import lafreakshow.plugins.valet.settings.ValetSettings.Companion.getInstance
import lafreakshow.plugins.valet.settings.ValetSettings.Companion.settingsDelegator
import lafreakshow.plugins.valet.util.readInstanceProperty
import kotlin.properties.ReadOnlyProperty

/**
 * Application service to access the settings for the plugin.
 *
 * Use [getInstance] to ... get an instance.
 *
 * Use[settingsDelegator] to directly adress settings by their name by delegate
 *
 * Use [settingsState] if you need to change settings.
 *
 * @constructor Create empty Valet settings
 */
@State(name = "ValetSettings", storages = [Storage("lafreakshow/TestValet.xml")])
class ValetSettings : PersistentStateComponent<ValetSettingsState> {
    var settingsState: ValetSettingsState = ValetSettingsState()
        private set

    companion object {
        /** Returns the instance of the service. */
        fun getInstance(): ValetSettings = ServiceManager.getService(ValetSettings::class.java)

        /**
         * get a delaggate to the setting with the given name. If no name is given, will use the name of the property
         * that is being delegated
         */
        fun <R : Any> settingsDelegator(name: String? = null): ReadOnlyProperty<Any, R> =
            ReadOnlyProperty<Any, R> { _, property ->
                // CHECK: Must it be assumed that IntelliJ may replace the service instance at any moment or is
                // it safe to store a reference?
                val settings = ServiceManager.getService(ValetSettings::class.java)
                settings.settingsState.readInstanceProperty<R>(name ?: property.name)
            }
    }

    override fun getState(): ValetSettingsState? = settingsState.copy()

    override fun loadState(state: ValetSettingsState) {
        settingsState = state.copy()
    }
}

/**
 * Contains the settings for the plugin.
 *
 * @property testSuffixes list of suffixes of test files or classes
 *
 */
data class ValetSettingsState(
    var testSuffixes: ArrayList<String> = arrayListOf("Test", "KtTest"),
)
