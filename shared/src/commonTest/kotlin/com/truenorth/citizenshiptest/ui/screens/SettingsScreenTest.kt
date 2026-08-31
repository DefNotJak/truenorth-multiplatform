package com.truenorth.citizenshiptest.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsScreenTest {

    @Test
    fun percentEncode_leavesUnreservedCharactersAlone() {
        assertEquals("abcXYZ019-_.~", percentEncode("abcXYZ019-_.~"))
    }

    @Test
    fun percentEncode_encodesSpacesAndNewlines() {
        assertEquals("a%20b", percentEncode("a b"))
        assertEquals("a%0Ab", percentEncode("a\nb"))
    }

    @Test
    fun percentEncode_encodesMailtoSpecialCharacters() {
        // These appear in the feedback mailto: URL's subject/body params - a broken
        // encoder here silently breaks the whole "Send Feedback" flow.
        assertEquals("a%3Fb", percentEncode("a?b"))
        assertEquals("a%26b", percentEncode("a&b"))
        assertEquals("a%3Db", percentEncode("a=b"))
    }

    @Test
    fun percentEncode_roundTripsAFullFeedbackBody() {
        val body = "\n\n\n---\nApp version: 1.0"
        val encoded = percentEncode(body)
        assertEquals("%0A%0A%0A---%0AApp%20version%3A%201.0", encoded)
    }

    @Test
    fun formatDateUtc_formatsAtUtcMidnightRegardlessOfLocalOffset() {
        // 2027-01-05T00:00:00Z
        assertEquals("Jan 5, 2027", formatDateUtc(1799107200000))
    }

    @Test
    fun formatDateUtc_handlesEndOfYear() {
        // 2026-12-31T00:00:00Z
        assertEquals("Dec 31, 2026", formatDateUtc(1798675200000))
    }
}
