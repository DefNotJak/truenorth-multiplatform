package com.truenorth.citizenshiptest.navigation

import com.truenorth.citizenshiptest.data.Category

object Routes {
    const val HOME = "home"
    const val PRACTICE_TEST_SETUP = "practice_test_setup"
    const val PRACTICE_TEST = "practice_test"
    const val STUDY = "study"
    const val FLASHCARD_DECK = "flashcard_deck/{categoryName}"
    const val FAVORITE_QUESTIONS = "favorite_questions"
    // Not a real NavHost destination - AppNavHost.onNavigate special-cases this
    // route to launch straight into a filtered Practice Test session instead of
    // navigating to a screen. Exists as a route string only so it can live as an
    // ordinary entry in HomeScreen's menuItems() list.
    const val SMART_REVIEW = "smart_review"
    const val PROGRESS = "progress"
    const val INFO = "info"
    const val SETTINGS = "settings"
    const val PAYWALL = "paywall"

    fun flashcardDeck(category: Category) = "flashcard_deck/${category.name}"
}
