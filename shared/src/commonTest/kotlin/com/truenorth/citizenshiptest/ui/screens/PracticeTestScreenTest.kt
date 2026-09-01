package com.truenorth.citizenshiptest.ui.screens

import kotlin.test.Test
import kotlin.test.assertEquals

class PracticeTestScreenTest {

    @Test
    fun formatDuration_padsSecondsButNotMinutes() {
        assertEquals("44:58", formatDuration(44 * 60_000 + 58_000))
        assertEquals("5:03", formatDuration(5 * 60_000 + 3_000))
        assertEquals("0:00", formatDuration(0))
    }

    @Test
    fun formatDuration_dropsSubSecondRemainder() {
        // 44:58.999 -> still reads as 44:58, not rounded up to 44:59 -
        // the timer must never show a value higher than the true remaining time.
        assertEquals("44:58", formatDuration(44 * 60_000 + 58_999))
    }

    @Test
    fun optionState_beforeReveal_alwaysNeutral() {
        assertEquals(OptionState.NEUTRAL, optionState(index = 0, correctIndex = 0, selectedIndex = 0, isRevealed = false))
        assertEquals(OptionState.NEUTRAL, optionState(index = 1, correctIndex = 0, selectedIndex = 1, isRevealed = false))
    }

    @Test
    fun optionState_afterReveal_marksCorrectAnswerGreen() {
        assertEquals(OptionState.CORRECT, optionState(index = 2, correctIndex = 2, selectedIndex = 0, isRevealed = true))
    }

    @Test
    fun optionState_afterReveal_marksWrongSelectedAnswerRed() {
        assertEquals(OptionState.INCORRECT, optionState(index = 0, correctIndex = 2, selectedIndex = 0, isRevealed = true))
    }

    @Test
    fun optionState_afterReveal_leavesUnselectedWrongAnswersNeutral() {
        assertEquals(OptionState.NEUTRAL, optionState(index = 1, correctIndex = 2, selectedIndex = 0, isRevealed = true))
    }

    @Test
    fun optionState_whenNothingWasSelected_correctIsStillHighlighted() {
        // Timer ran out with no answer chosen - selectedIndex is null.
        assertEquals(OptionState.CORRECT, optionState(index = 2, correctIndex = 2, selectedIndex = null, isRevealed = true))
        assertEquals(OptionState.NEUTRAL, optionState(index = 0, correctIndex = 2, selectedIndex = null, isRevealed = true))
    }
}
