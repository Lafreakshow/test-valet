package simple.kotlin

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class SimpleKtClassTest {

    @Test
    fun dumbTestMethod() {
        assertEquals(50, SimpleKtClass().dumbTestMethod())
    }
}