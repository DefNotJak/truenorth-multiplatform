package com.truenorth.citizenshiptest.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthScreenTest {

    @Test
    fun passwordComplexityError_tooShort_isRejectedRegardlessOfContent() {
        assertEquals("Password must be at least 8 characters.", passwordComplexityError("ab1"))
    }

    @Test
    fun passwordComplexityError_noDigit_isRejected() {
        assertEquals("Password must include at least one number.", passwordComplexityError("abcdefgh"))
    }

    @Test
    fun passwordComplexityError_noLetter_isRejected() {
        assertEquals("Password must include at least one letter.", passwordComplexityError("12345678"))
    }

    @Test
    fun passwordComplexityError_meetsAllRules_isAccepted() {
        assertNull(passwordComplexityError("abcd1234"))
    }

    @Test
    fun passwordComplexityError_exactlyEightChars_isAccepted() {
        assertNull(passwordComplexityError("a1234567"))
    }
}
