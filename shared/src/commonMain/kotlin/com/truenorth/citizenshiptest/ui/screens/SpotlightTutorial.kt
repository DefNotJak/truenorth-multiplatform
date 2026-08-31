package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.truenorth.citizenshiptest.navigation.Routes

data class TutorialTarget(val route: String, val title: String, val description: String)

val homeTutorialTargets = listOf(
    TutorialTarget(
        route = Routes.PRACTICE_TEST_SETUP,
        title = "Practice Test",
        description = "Tap to start instantly with your last settings, styled like the real citizenship test with a live 45-minute timer. Use the icon on the right to change topics or question count."
    ),
    TutorialTarget(
        route = Routes.STUDY,
        title = "Flash Cards",
        description = "Study every fact at your own pace, organized by category - always free, no test pressure."
    ),
    TutorialTarget(
        route = Routes.FAVORITE_QUESTIONS,
        title = "Favourite Questions",
        description = "Star any question while studying or reviewing a test, then revisit everything you've starred in one place. A premium feature - upgrade anytime from Settings."
    ),
    TutorialTarget(
        route = Routes.SMART_REVIEW,
        title = "Smart Review",
        description = "Automatically tracks every question you've gotten wrong across all your tests, so you can drill just your weak spots instead of starting from scratch. Also premium."
    ),
    TutorialTarget(
        route = Routes.PROGRESS,
        title = "Progress",
        description = "Track your scores and accuracy over time, so you always know exactly where you stand."
    )
)

/**
 * Coach-mark style walkthrough: dims the real Home screen and punches a
 * transparent cutout around each target's actual on-screen button in turn,
 * rather than describing features in a generic floating card. Requires
 * [menuItemBounds] (populated by HomeScreen via onGloballyPositioned on each
 * MenuCard) and [listIndexForRoute] so each step can scroll its target into
 * view before highlighting it.
 */
@Composable
fun SpotlightTutorial(
    menuItemBounds: Map<String, Rect>,
    listState: LazyListState,
    listIndexForRoute: (String) -> Int,
    onFinish: () -> Unit
) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val target = homeTutorialTargets[stepIndex]
    val isLast = stepIndex == homeTutorialTargets.size - 1

    LaunchedEffect(stepIndex) {
        listState.animateScrollToItem(listIndexForRoute(target.route))
    }

    val bounds = menuItemBounds[target.route]
    val density = LocalDensity.current
    val screenHeightDp = LocalWindowInfo.current.containerSize.height / density.density

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawRect(color = Color.Black.copy(alpha = 0.78f))
                    bounds?.let { cutoutRect ->
                        val cutoutPadding = 12.dp.toPx()
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = Offset(cutoutRect.left - cutoutPadding, cutoutRect.top - cutoutPadding),
                            size = Size(cutoutRect.width + cutoutPadding * 2, cutoutRect.height + cutoutPadding * 2),
                            cornerRadius = CornerRadius(20.dp.toPx()),
                            blendMode = BlendMode.Clear
                        )
                    }
                }
                .pointerInput(stepIndex) {
                    detectTapGestures {
                        if (isLast) onFinish() else stepIndex += 1
                    }
                }
        )

        if (bounds != null) {
            val bottomDp = with(density) { bounds.bottom.toDp() }
            val topDp = with(density) { bounds.top.toDp() }
            val showBelow = bottomDp.value < screenHeightDp - 220
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 24.dp)
                    .padding(top = if (showBelow) bottomDp + 16.dp else (topDp - 150.dp).coerceAtLeast(56.dp))
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = target.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4A4A4A),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = if (isLast) "Tap anywhere to finish" else "Tap anywhere to continue",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF8A8A8A),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }

        TextButton(
            onClick = onFinish,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp)
        ) {
            Text("Skip", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
