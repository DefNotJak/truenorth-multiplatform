package com.truenorth.citizenshiptest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavType
import androidx.savedstate.read
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.truenorth.citizenshiptest.data.AuthRepository
import com.truenorth.citizenshiptest.data.BillingProducts
import com.truenorth.citizenshiptest.data.Category
import com.truenorth.citizenshiptest.data.CompletedPurchase
import com.truenorth.citizenshiptest.data.FavoriteQuestionsRepository
import com.truenorth.citizenshiptest.data.FlashcardReviewRepository
import com.truenorth.citizenshiptest.data.ProductPrice
import com.truenorth.citizenshiptest.data.PurchaseUpdate
import com.truenorth.citizenshiptest.data.QuestionReportRepository
import com.truenorth.citizenshiptest.data.ThemeMode
import com.truenorth.citizenshiptest.data.UsageRepository
import com.truenorth.citizenshiptest.data.UsageState
import com.truenorth.citizenshiptest.data.HomeStats
import com.truenorth.citizenshiptest.data.rememberBillingRepository
import com.truenorth.citizenshiptest.data.rememberPracticeTestPreferencesRepository
import com.truenorth.citizenshiptest.data.rememberReviewPromptRepository
import com.truenorth.citizenshiptest.data.rememberTestResultsRepository
import com.truenorth.citizenshiptest.ui.screens.FavoriteQuestionsScreen
import com.truenorth.citizenshiptest.ui.screens.FlashcardDeckScreen
import com.truenorth.citizenshiptest.ui.screens.HomeScreen
import com.truenorth.citizenshiptest.ui.screens.InfoScreen
import com.truenorth.citizenshiptest.ui.screens.PaywallScreen
import com.truenorth.citizenshiptest.ui.screens.PracticeTestConfig
import com.truenorth.citizenshiptest.ui.screens.PracticeTestScreen
import com.truenorth.citizenshiptest.ui.screens.PracticeTestSetupScreen
import com.truenorth.citizenshiptest.ui.screens.ProgressScreen
import com.truenorth.citizenshiptest.ui.screens.SettingsScreen
import com.truenorth.citizenshiptest.ui.screens.StudyScreen
import kotlinx.coroutines.launch

private const val SMART_REVIEW_MAX_QUESTIONS = 30

@Composable
fun AppNavHost(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    userId: String,
    userEmail: String?,
    authRepository: AuthRepository,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val repository = rememberTestResultsRepository()
    val usageRepository = remember(userId) { UsageRepository(userId) }
    val favoritesRepository = remember(userId) { FavoriteQuestionsRepository(userId) }
    val reviewRepository = remember(userId) { FlashcardReviewRepository(userId) }
    val reportRepository = remember(userId) { QuestionReportRepository(userId) }
    val practiceTestPreferences = rememberPracticeTestPreferencesRepository()
    val savedPracticeConfig by practiceTestPreferences.config.collectAsState(initial = PracticeTestConfig())
    val reviewPromptRepository = rememberReviewPromptRepository()
    var isEmailVerified by remember(userId) { mutableStateOf(authRepository.currentUserSnapshot?.isEmailVerified ?: false) }
    val scope = rememberCoroutineScope()
    val usageState by usageRepository.usageState.collectAsState(
        initial = UsageState(freeTestsUsedToday = 0, freeTestDate = null, subscriptionExpiresAtMillis = null)
    )

    val billingRepository = rememberBillingRepository()
    var productPrices by remember { mutableStateOf<Map<String, ProductPrice>>(emptyMap()) }

    // Grants entitlement for every purchase reported as complete, then consumes/
    // finishes it so the same one-time product can be bought again once the
    // time-limited pass expires. Only pops the Paywall if the user is actually on
    // it - a Success here can also come from startup/resume reconciliation while
    // the user is anywhere else in the app.
    suspend fun handlePurchases(purchases: List<CompletedPurchase>) {
        var grantedAny = false
        for (purchase in purchases) {
            val durationMillis = BillingProducts.DURATIONS_MILLIS[purchase.productId] ?: continue
            try {
                usageRepository.grantPass(purchase.purchaseToken, durationMillis)
                billingRepository.consumePurchase(purchase)
                grantedAny = true
            } catch (e: Exception) {
                // Leave unconsumed - the next reconciliation pass (resume/cold
                // start) will retry.
            }
        }
        if (grantedAny && navController.currentDestination?.route == Routes.PAYWALL) {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        billingRepository.connect()
        productPrices = billingRepository.queryProductPrices()
        handlePurchases(billingRepository.queryExistingPurchases())
        billingRepository.purchaseUpdates.collect { update ->
            when (update) {
                is PurchaseUpdate.Success -> handlePurchases(update.purchases)
                is PurchaseUpdate.UserCancelled -> Unit
                is PurchaseUpdate.Error -> Unit
            }
        }
    }

    // A purchase can complete outside the live process (e.g. the user backgrounds
    // the app during the store's own purchase UI), so reconcile again on every
    // resume, not just once at cold start.
    LifecycleResumeEffect(Unit) {
        scope.launch {
            billingRepository.connect()
            handlePurchases(billingRepository.queryExistingPurchases())
        }
        onPauseOrDispose { }
    }

    val onSelectPlan: (String) -> Unit = { productId -> billingRepository.launchPurchase(productId) }

    val homeStats by repository.observeHomeStats()
        .collectAsState(initial = HomeStats(testsTaken = 0, averageScorePercent = null, accuracyPercent = null))
    val missedQuestionIds by repository.observeMissedQuestionIds().collectAsState(initial = emptySet())
    val favoriteQuestionIds by favoritesRepository.favoriteQuestionIds.collectAsState(initial = emptySet())
    val reviewedQuestionIds by reviewRepository.reviewedQuestionIds.collectAsState(initial = emptySet())
    val onCardViewed: (Int) -> Unit = { questionId ->
        if (questionId !in reviewedQuestionIds) {
            scope.launch { reviewRepository.markReviewed(questionId) }
        }
    }
    val onToggleFavorite: (Int) -> Unit = { questionId ->
        if (usageState.hasActiveSubscription) {
            scope.launch { favoritesRepository.setFavorite(questionId, questionId !in favoriteQuestionIds) }
        } else {
            navController.navigate(Routes.PAYWALL)
        }
    }
    val onSetTestDate: (Long?) -> Unit = { millis -> scope.launch { usageRepository.setTestDate(millis) } }
    val onOnboardingCompleted: () -> Unit = { scope.launch { usageRepository.markOnboardingCompleted() } }
    val reportedQuestionIds by reportRepository.reportedQuestionIds.collectAsState(initial = emptySet())
    val onReportQuestion: (Int, String, String) -> Unit = { questionId, reason, note ->
        scope.launch {
            // A failed write here (permissions, network) shouldn't crash the app over
            // a best-effort content-QA signal - the user already sees "Reported" as
            // soon as they submit, so there's no in-app feedback loop to update either.
            try {
                reportRepository.submitReport(questionId, reason, note)
            } catch (e: Exception) {
                // Swallow - reporting a question is non-critical.
            }
        }
    }
    var pendingTestConfig by remember { mutableStateOf(PracticeTestConfig()) }
    val onUpgradeClick: () -> Unit = { navController.navigate(Routes.PAYWALL) }
    val onStartPracticeTest: () -> Unit = {
        if (usageState.hasFreeTestsRemaining) {
            pendingTestConfig = savedPracticeConfig
            navController.navigate(Routes.PRACTICE_TEST)
        } else {
            navController.navigate(Routes.PAYWALL)
        }
    }
    val onCustomizePracticeTest: () -> Unit = {
        if (usageState.hasFreeTestsRemaining) {
            navController.navigate(Routes.PRACTICE_TEST_SETUP)
        } else {
            navController.navigate(Routes.PAYWALL)
        }
    }
    val onStartSmartReview: () -> Unit = {
        if (!usageState.hasActiveSubscription) {
            navController.navigate(Routes.PAYWALL)
        } else if (missedQuestionIds.isNotEmpty()) {
            pendingTestConfig = PracticeTestConfig(
                questionCount = minOf(SMART_REVIEW_MAX_QUESTIONS, missedQuestionIds.size),
                restrictToIds = missedQuestionIds
            )
            navController.navigate(Routes.PRACTICE_TEST)
        }
        // Subscribed with 0 missed questions: no-op, the Home card already reads
        // "You're all caught up" so there's nothing useful to launch into.
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val homeStats by repository.observeHomeStats()
                .collectAsState(initial = HomeStats(testsTaken = 0, averageScorePercent = null, accuracyPercent = null))
            HomeScreen(
                stats = homeStats,
                testDateMillis = usageState.testDateMillis,
                onboardingCompleted = usageState.onboardingCompleted,
                privacyConsentGiven = usageState.privacyConsentGiven,
                isDataLoaded = usageState.isLoaded,
                hasActiveSubscription = usageState.hasActiveSubscription,
                missedQuestionCount = missedQuestionIds.size,
                onSetTestDate = { millis -> onSetTestDate(millis) },
                onOnboardingCompleted = onOnboardingCompleted,
                onPrivacyConsentGiven = { scope.launch { usageRepository.markPrivacyConsentGiven() } },
                onUpgradeClick = onUpgradeClick,
                onStartPracticeTest = onStartPracticeTest,
                onCustomizePracticeTest = onCustomizePracticeTest,
                onNavigate = { route ->
                    val needsPaywall = route == Routes.FAVORITE_QUESTIONS && !usageState.hasActiveSubscription
                    when {
                        route == Routes.SMART_REVIEW -> onStartSmartReview()
                        needsPaywall -> navController.navigate(Routes.PAYWALL)
                        else -> navController.navigate(route)
                    }
                }
            )
        }
        composable(Routes.PRACTICE_TEST_SETUP) {
            PracticeTestSetupScreen(
                initialConfig = savedPracticeConfig,
                onStart = { config ->
                    pendingTestConfig = config
                    scope.launch { practiceTestPreferences.saveConfig(config) }
                    navController.navigate(Routes.PRACTICE_TEST)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PRACTICE_TEST) {
            PracticeTestScreen(
                config = pendingTestConfig,
                favoriteQuestionIds = favoriteQuestionIds,
                onToggleFavorite = onToggleFavorite,
                reportedQuestionIds = reportedQuestionIds,
                onReportQuestion = onReportQuestion,
                hasActiveSubscription = usageState.hasActiveSubscription,
                onUpgradeClick = onUpgradeClick,
                onBack = { navController.popBackStack() },
                onTestSaved = { correctCount, total, categoryTallies, questionResults ->
                    // Captured before the save below, not read after it - the Flow
                    // backing homeStats can reactively update fast enough that reading
                    // it post-save sometimes already reflects *this* attempt, not
                    // just prior ones, which would let the review ask fire on the
                    // very first test instead of the second.
                    val testsTakenBeforeThisOne = homeStats.testsTaken
                    scope.launch {
                        repository.saveAttempt(correctCount, total, categoryTallies)
                        repository.recordQuestionResults(questionResults)
                        usageRepository.incrementFreeTestsUsed()
                        // Ask at a deliberately good moment - a pass, not the very
                        // first test - rather than on every completion.
                        val passed = total > 0 && (correctCount * 100 / total) >= 75
                        if (passed && testsTakenBeforeThisOne >= 1 && !reviewPromptRepository.hasRequestedReview()) {
                            reviewPromptRepository.markReviewRequested()
                            reviewPromptRepository.requestReviewFlow()
                        }
                    }
                }
            )
        }
        composable(Routes.STUDY) {
            val categoryBreakdown by repository.observeCategoryBreakdown().collectAsState(initial = emptyList())
            StudyScreen(
                reviewedQuestionIds = reviewedQuestionIds,
                categoryBreakdown = categoryBreakdown,
                onCategoryClick = { category -> navController.navigate(Routes.flashcardDeck(category)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.FLASHCARD_DECK,
            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.read { getStringOrNull("categoryName") }
            val category = Category.entries.first { it.name == categoryName }
            FlashcardDeckScreen(
                category = category,
                favoriteQuestionIds = favoriteQuestionIds,
                onToggleFavorite = onToggleFavorite,
                reportedQuestionIds = reportedQuestionIds,
                onReportQuestion = onReportQuestion,
                onCardViewed = onCardViewed,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.FAVORITE_QUESTIONS) {
            FavoriteQuestionsScreen(
                favoriteQuestionIds = favoriteQuestionIds,
                onToggleFavorite = onToggleFavorite,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PROGRESS) {
            val homeStats by repository.observeHomeStats()
                .collectAsState(initial = HomeStats(testsTaken = 0, averageScorePercent = null, accuracyPercent = null))
            val categoryBreakdown by repository.observeCategoryBreakdown().collectAsState(initial = emptyList())
            val scoreHistory by repository.observeScoreHistory().collectAsState(initial = emptyList())
            ProgressScreen(
                stats = homeStats,
                categoryBreakdown = categoryBreakdown,
                scoreHistory = scoreHistory,
                onCategoryClick = { category -> navController.navigate(Routes.flashcardDeck(category)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.INFO) {
            InfoScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                onBack = { navController.popBackStack() },
                onUpgradeClick = onUpgradeClick,
                userEmail = userEmail,
                isEmailVerified = isEmailVerified,
                onSignOut = onSignOut,
                usageState = usageState,
                onSetTestDate = onSetTestDate,
                onDeleteAccount = {
                    usageRepository.deleteUserData()
                    repository.clearAllData()
                    authRepository.deleteAccount()
                },
                onReauthenticate = { password -> authRepository.reauthenticate(password) },
                onResendVerificationEmail = { authRepository.sendEmailVerification() },
                onRefreshVerificationStatus = {
                    authRepository.reloadCurrentUser()
                    isEmailVerified = authRepository.currentUserSnapshot?.isEmailVerified ?: false
                }
            )
        }
        composable(Routes.PAYWALL) {
            PaywallScreen(
                productPrices = productPrices,
                onSelectPlan = onSelectPlan,
                onClose = { navController.popBackStack() }
            )
        }
    }
}
