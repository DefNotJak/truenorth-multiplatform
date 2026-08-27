package com.truenorth.citizenshiptest.data

enum class QuestionType { MULTIPLE_CHOICE, TRUE_FALSE }

enum class Category(val displayName: String) {
    RIGHTS_RESPONSIBILITIES("Rights & Responsibilities"),
    WHO_WE_ARE("Who We Are"),
    HISTORY("Canada's History"),
    GOVERNMENT("How Canadians Govern"),
    SYMBOLS("Canadian Symbols"),
    ECONOMY_GEOGRAPHY("Economy & Geography")
}

data class Question(
    val id: Int,
    val category: Category,
    val type: QuestionType,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    // Questions that test the same underlying fact share a topicGroupId, so
    // practice-test selection picks at most one variant per group - no two
    // questions about the same fact appear in a single test, and repeated
    // attempts naturally rotate which phrasing shows up. Defaults to a
    // unique value per question, so existing single-variant questions need
    // no changes; only give two or more questions the same topicGroupId
    // when they are genuinely interchangeable variants of one fact.
    val topicGroupId: String = "q$id",
    // Some multiple-choice questions are phrased as "Which of the following
    // is..." - that reads fine in Practice Test, where all 4 options are
    // shown, but falls apart in Flash Cards, where only the correct option
    // is shown alongside the question. flashcardText, when set, is a
    // standalone-statement rewording used only by Flash Cards; Practice
    // Test always uses text, unaffected.
    val flashcardText: String? = null
)
