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

package lafreakshow.plugins.valet

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.diagnostic.Logger
import lafreakshow.plugins.valet.util.logger

class DynamicReloadListener: DynamicPluginListener {
    private val log: Logger by logger()

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        log.info("${pluginDescriptor.name} loaded...")
    }

    override fun pluginUnloaded(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        log.info("${pluginDescriptor.name} unloaded${if (isUpdate) " as part of update" else ""}...")
    }
}
