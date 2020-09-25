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
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.openapi.roots.TestModuleProperties
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

// Logging Stuff
// =============

/** Returns a logger with the name of the receiver class. */
fun <R : Any> R.logger(): Lazy<Logger> = lazy { Logger.getInstance(this::class.java) }

/** Logs a property. See [toDebugString] for details */
fun <T : Any> Logger.logValue(prop: KProperty0<T>) {
    this.debug(prop.toDebugString())
}

/** Logs the  string returned by provider at the debug level. Provider will only be called if debug level is enabled. */
fun Logger.debug(provider: () -> String) {
    if (this.isDebugEnabled) this.debug(provider())
}

/** Logs the  string returned by provider at the trace level. Provider will only be called if trace level is enabled. */
fun Logger.trace(provider: () -> String) {
    if (this.isTraceEnabled) this.trace(provider())
}

/** Like [logValue] but outputs at trace level. */
fun <R> Logger.trace(prop: KProperty0<R>) {
    if (this.isTraceEnabled) this.trace(prop.toDebugString())
}

// Debugging Extensions
// ====================

/**
 * Build a string of the form:
 *     [var|val] [receiverClass.simpleName].[property.name]:[Type] = [property value].
 */
fun <R> KProperty0<R>.toDebugString(): String = buildString {
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

/** Returns a string representation of a given KType. */
private fun typeToString(type: KType): String = if (type.classifier is KClass<*>) {
    val arguments = if (type.arguments.isNotEmpty()) {
        type.arguments.joinToString(", ", "<", ">") {
            it.type?.let { it1 -> typeToString(it1) } ?: "<*>"
        }
    } else ""

    "${(type.classifier as KClass<*>).simpleName}$arguments${if (type.isMarkedNullable) "?" else ""}"
} else {
    type.toString()
}

// Convenience Stuff
/**
 * Get the value of a property by it's name. Will not perform any checks.
 *
 * @throws NoSuchElementException if a property with the given name does not exist.
 * @throws ClassCastException if the property's value cannot be cast to [R]
 */
@Suppress("UNCHECKED_CAST")
fun <R : Any> Any.readInstanceProperty(name: String): R {
    val memProp = this::class.memberProperties.first { it.name == name } as KProperty1<Any, *>
    return memProp.get(this) as R
}

/**
 * Returns a search scope that contains the given [element] and if possible related test/source files.
 *
 * Will attempt to find a production module belonging to the [element]. If the [element] is not in a test module or the
 * module has not associated production module, will return the search context for the module [element] belongs to
 * including any dependents and test sources instead.
 *
 * Will return an empty Optional if [element] does not belong to any module.
 * */
fun getModuleSearchScope(element: PsiElement): Optional<GlobalSearchScope> {
    val vFile = element.containingFile.virtualFile
    val project = element.project

    val module = FileIndexFacade.getInstance(project).getModuleForFile(vFile)

    return if (module != null) {
        val testService = module.getService(TestModuleProperties::class.java)
        // Use the service to see if there may be a production module belonging to the elements module
        // If there is no production module defined then use the scope of the elements module directly.
        val scope = testService.productionModule?.moduleScope ?: module.moduleTestsWithDependentsScope

        Optional.of(scope)
    } else {
        Optional.empty()
    }
}
