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

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.reflect.KProperty0

internal class DummyClass(val value: Int = 2)
internal class DummyClassWithToString(private val value: Int = 5) {
    override fun toString(): String = "<DummyClasseWithToString value: $value>"
}

internal class ExtensionsTest {
    private val propString = "test"
    private val propInt = 2
    private val propDouble = 3.5
    private val propBool = true

    private val propStringN: String? = "test"
    private val propStringN2: String? = null
    private val propIntN: Int? = 2
    private val propDoubleN: Double? = 3.5
    private val propBoolN: Boolean? = true

    private val propObject = DummyClass()
    private val propObjectWithString = DummyClassWithToString()

    private var varPropString = "var test"
    private var varPropInt = 4
    private var varlistProp = listOf("test1", "test2", "test3")
    private var varmapProp = mapOf("test1" to 3, "test2" to 4, "test5" to 7)

    private val listProp = listOf("test1", "test2", "test3")
    private val mapProp = mapOf("test1" to 3, "test2" to 4, "test5" to 7)

    private val mutableListProp: MutableList<String> = mutableListOf("test1", "test2", "test3")
    private val mutableMapProp: MutableMap<String, Int> = mutableMapOf("test1" to 3, "test2" to 4, "test5" to 7)


    @TestFactory
    fun toDebugString(): List<DynamicTest> {
        mutableListProp.add("54")

        fun with(
            prop: KProperty0<*>, isVal: Boolean, typeString: String,
            expectedReceiver: String = this::class.simpleName!!,
        ): DynamicTest {
            val expected = "${if (isVal) "val" else "var"} $expectedReceiver.${prop.name}: $typeString = ${prop.get()}"
            return DynamicTest.dynamicTest(expected) {
                expectThat(prop.toDebugString()).isEqualTo(expected)
            }
        }

        return listOf(
            with(this::propString, true, "String"),
            with(this::propInt, true, "Int"),
            with(this::propDouble, true, "Double"),
            with(this::propBool, true, "Boolean"),

            with(this::propStringN, true, "String?"),
            with(this::propStringN2, true, "String?"),
            with(this::propIntN, true, "Int?"),
            with(this::propDoubleN, true, "Double?"),
            with(this::propBoolN, true, "Boolean?"),

            with(this::varPropString, false, "String"),
            with(this::varPropInt, false, "Int"),

            with(this::varlistProp, false, "List<String>"),
            with(this::varmapProp, false, "Map<String, Int>"),

            with(this::propObject, true, "DummyClass"),
            with(this::propObjectWithString, true, "DummyClassWithToString"),

            with(this::listProp, true, "List<String>"),
            with(this::mapProp, true, "Map<String, Int>"),

            // It's not all that easy to get Mutable* out of KType so we settle for the common type.
            with(this::mutableListProp, true, "List<String>"),
            with(this::mutableMapProp, true, "Map<String, Int>"),
        )
    }
}
