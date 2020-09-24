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
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

// Logging Stuff
// =============

fun <R : Any> R.logger() = lazy { Logger.getInstance(this::class.java) }
fun <T : Any> Logger.logValue(prop: KProperty0<T>) {
    this.debug(prop.toDebugString())
}

fun Logger.debug(provider: () -> String) {
    if (this.isDebugEnabled) this.debug(provider())
}

fun Logger.trace(provider: () -> String) {
    if (this.isTraceEnabled) this.trace(provider())
}

fun <R> Logger.trace(prop: KProperty0<R>) {
    if (this.isTraceEnabled) this.trace(prop.toDebugString())
}

// Debugging Extensions
// ====================

/**
 * Build a string of the form:
 *     [var|val] [receiverClass.simpleName].[property.name]:[Type] = [property value].
 */
fun <R> KProperty0<R>.toDebugString() = buildString {
    val prop = this@toDebugString
    if (prop is KMutableProperty<*>) {
        append("var")
    } else {
        append("val")
    }

    append(" ").append(prop.javaField?.declaringClass?.kotlin?.simpleName).append(".").append(prop.name)
    append(": ").append(typeToString(prop.returnType))
    append(" = ").append(prop.get())
}

@OptIn(ExperimentalStdlibApi::class)
private fun typeToString(type: KType): String {
    if (type.classifier is KClass<*>) {
        val arguments = if (type.arguments.isNotEmpty()) {
            type.arguments.joinToString(", ", "<", ">") {
                it.type?.let { it1 -> typeToString(it1) } ?: "<*>"
            }
        } else ""

        return "${(type.classifier as KClass<*>).simpleName}$arguments${if (type.isMarkedNullable) "?" else ""}"
    } else {
        return type.toString()
    }
}

// Convenience Stuff
fun <R : Any> Any.readInstanceProperty(name: String): R {
    val memProp = this::class.memberProperties.first { it.name == name } as KProperty1<Any, *>
    return memProp.get(this) as R
}
