package com.truenorth.citizenshiptest.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals

class StudyScreenTest {

    @Test
    fun reviewStatusLabel_zeroReviewed_isNotStarted() {
        assertEquals("Not Started", reviewStatusLabel(reviewedCount = 0, cardCount = 74))
    }

    @Test
    fun reviewStatusLabel_someReviewed_isInProgress() {
        assertEquals("In Progress", reviewStatusLabel(reviewedCount = 10, cardCount = 74))
    }

    @Test
    fun reviewStatusLabel_allReviewed_isReviewed() {
        assertEquals("Reviewed", reviewStatusLabel(reviewedCount = 74, cardCount = 74))
    }

    @Test
    fun reviewStatusLabel_reviewedExceedsCardCount_stillReadsAsReviewed() {
        // Can happen if a category's question set shrinks after a user has already
        // reviewed cards from it (a content update removing a question).
        assertEquals("Reviewed", reviewStatusLabel(reviewedCount = 80, cardCount = 74))
    }

    @Test
    fun reviewStatusLabel_emptyCategory_isNotStarted() {
        assertEquals("Not Started", reviewStatusLabel(reviewedCount = 0, cardCount = 0))
    }
}
