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

package lafreakshow.plugins.valet.util

import com.intellij.openapi.diagnostic.Logger
import org.intellij.markdown.flavours.gfm.table.GitHubTableMarkerProvider.Companion.contains
import org.jetbrains.builtInWebServer.validateToken
import org.jetbrains.kotlin.idea.debugger.isInlineFunctionLineNumber
import org.jetbrains.kotlin.resolve.calls.components.checkSimpleArgument
import org.jetbrains.kotlin.scripting.resolve.classId
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf


// Logging Stuff
// =============

fun <R : Any> R.logger() = lazy { Logger.getInstance(this::class.java) }
fun <T : Any> Logger.logValue(prop: KProperty0<T>) {
    this.debug(prop.toDebugString())
}

fun Logger.debug(provider: () -> String) {
    if (this.isDebugEnabled) this.debug(provider())
}

// Debugging Extensions
// ====================

/**
 * Build a string of the form:
 *     [var|val] [receiverClass.simpleName].[property.name]:[Type] = [property value]
 */
fun <R> KProperty0<R>.toDebugString() = buildString {
    val prop = this@toDebugString
    if (prop is KMutableProperty<*>)
        append("var")
    else
        append("val")

    append(" ").append(prop.javaField?.declaringClass?.kotlin?.simpleName).append(".").append(prop.name)
    append(": ").append(typeToString(prop.returnType))
    append(" = ").append(prop.get())
}

@OptIn(ExperimentalStdlibApi::class)
private fun typeToString(type: KType): String {
    if (type.classifier is KClass<*>) {
        val arguments = if (type.arguments.isNotEmpty())
            type.arguments.joinToString(", ", "<", ">") {
                it.type?.let { it1 -> typeToString(it1) } ?: "<*>"
            }
        else ""

        return "${(type.classifier as KClass<*>).simpleName}$arguments${if (type.isMarkedNullable) "?" else ""}"
    } else
        return type.toString()
}
