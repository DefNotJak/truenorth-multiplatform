package com.truenorth.citizenshiptest.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuestionBankTest {

    @Test
    fun byIds_returnsQuestionsInRequestedOrder_skippingUnknownIds() {
        val realIds = QuestionBank.all.take(3).map { it.id }

        val result = QuestionBank.byIds(realIds + listOf(-999))

        assertEquals(realIds, result.map { it.id })
    }

    @Test
    fun flashcardEligible_excludesFalseTrueFalseQuestions() {
        val category = Category.RIGHTS_RESPONSIBILITIES

        val eligible = QuestionBank.flashcardEligible(category)

        assertTrue(eligible.isNotEmpty())
        assertTrue(eligible.all { it.category == category })
        assertTrue(
            eligible.none { it.type == QuestionType.TRUE_FALSE && it.options[it.correctAnswerIndex] == "False" },
            "a 'the statement is False' flashcard would teach the wrong fact"
        )
    }

    @Test
    fun flashcardEligible_keepsTrueAnsweredTrueFalseQuestions() {
        val trueAnswered = QuestionBank.all.first {
            it.type == QuestionType.TRUE_FALSE && it.options[it.correctAnswerIndex] == "True"
        }

        val eligible = QuestionBank.flashcardEligible(trueAnswered.category)

        assertTrue(eligible.any { it.id == trueAnswered.id })
    }

    @Test
    fun matchingQuestionCount_isPositiveForEveryCategory() {
        Category.entries.forEach { category ->
            assertTrue(
                QuestionBank.matchingQuestionCount(setOf(category), null) > 0,
                "category $category has zero matching topic groups"
            )
        }
    }

    @Test
    fun matchingQuestionCount_nullFilters_matchesTotalDistinctTopicGroups() {
        val expected = QuestionBank.all.map { it.topicGroupId }.distinct().size

        assertEquals(expected, QuestionBank.matchingQuestionCount(null, null))
    }

    @Test
    fun matchingQuestionCount_emptySetBehavesLikeNull() {
        assertEquals(
            QuestionBank.matchingQuestionCount(null, null),
            QuestionBank.matchingQuestionCount(emptySet(), null)
        )
    }

    @Test
    fun customTestSet_neverReturnsTwoQuestionsFromTheSameTopicGroup() {
        // Randomized (shuffled/random pick) - repeat to reduce the chance of a
        // rare bad draw hiding a real bug.
        repeat(20) {
            val result = QuestionBank.customTestSet(count = 30)
            val topicGroupIds = result.map { it.topicGroupId }
            assertEquals(
                topicGroupIds.distinct().size,
                topicGroupIds.size,
                "duplicate topic group in a single test - two variants of the same fact appeared together"
            )
        }
    }

    @Test
    fun customTestSet_respectsCategoryFilter() {
        val category = Category.SYMBOLS

        val result = QuestionBank.customTestSet(count = 100, categories = setOf(category))

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.category == category })
    }

    @Test
    fun customTestSet_respectsQuestionTypeFilter() {
        val result = QuestionBank.customTestSet(count = 100, questionType = QuestionType.TRUE_FALSE)

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.type == QuestionType.TRUE_FALSE })
    }

    @Test
    fun customTestSet_respectsRestrictToIds() {
        val allowedIds = QuestionBank.all.take(5).map { it.id }.toSet()

        val result = QuestionBank.customTestSet(count = 20, restrictToIds = allowedIds)

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.id in allowedIds })
    }

    @Test
    fun customTestSet_returnsFewerThanRequestedWhenTheFilteredPoolIsSmaller() {
        val allowedIds = QuestionBank.all.take(2).map { it.id }.toSet()

        val result = QuestionBank.customTestSet(count = 20, restrictToIds = allowedIds)

        assertTrue(result.size <= 2, "asked for a pool of only 2 questions but got ${result.size}")
    }

    @Test
    fun customTestSet_emptyRestrictToIds_returnsNothing() {
        val result = QuestionBank.customTestSet(count = 20, restrictToIds = emptySet())

        assertTrue(result.isEmpty())
    }
}
