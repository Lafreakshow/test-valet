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
import lafreakshow.plugins.valet.util.readInstanceProperty
import kotlin.properties.ReadOnlyProperty

@State(name = "ValetSettings", storages = [Storage("lafreakshow/TestValet.xml")])
class ValetSettings : PersistentStateComponent<ValetSettingsState> {
    var settingsSstate = ValetSettingsState()
        private set

    companion object {
        fun getInstance() = ServiceManager.getService(ValetSettings::class.java)
        fun <R : Any> settingsDelegator(name: String? = null): ReadOnlyProperty<Any, R> =
            // CHECK: Must it be assumed that IntelliJ may replace the service instance at any moment or is
            ReadOnlyProperty<Any, R> { thisRef, property -> //  it save to store a reference?
                val settings = ServiceManager.getService(ValetSettings::class.java)
                settings.settingsSstate.readInstanceProperty<R>(name ?: property.name)
            }
    }

    override fun getState(): ValetSettingsState? = settingsSstate.copy()

    override fun loadState(state: ValetSettingsState) {
        settingsSstate = state.copy()
    }
}

data class ValetSettingsState(
    var testSuffixes: ArrayList<String> = arrayListOf("Test", "KtTest"),
    var maximumDebugMode: Boolean = true,
) {
}
