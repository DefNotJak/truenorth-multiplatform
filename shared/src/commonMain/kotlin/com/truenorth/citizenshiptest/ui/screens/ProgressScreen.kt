package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.truenorth.citizenshiptest.data.Category
import com.truenorth.citizenshiptest.data.CategoryBreakdown
import com.truenorth.citizenshiptest.data.HomeStats
import com.truenorth.citizenshiptest.data.ScorePoint

private const val PASS_THRESHOLD_PERCENT = 75
private val CorrectGreen = Color(0xFF2E7D32)
private val IncorrectRed = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    stats: HomeStats,
    categoryBreakdown: List<CategoryBreakdown>,
    scoreHistory: List<ScorePoint>,
    onCategoryClick: (Category) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (stats.testsTaken == 0) {
            EmptyProgressState(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SectionCard(title = "Overall Performance") {
                    AccuracySummary(stats = stats)
                }
            }
            item {
                SectionCard(title = "By Category") {
                    CategoryBarChart(breakdown = categoryBreakdown, onCategoryClick = onCategoryClick)
                }
            }
            item {
                SectionCard(title = "Score Over Time") {
                    ScoreLineChart(history = scoreHistory)
                }
            }
        }
    }
}

@Composable
private fun EmptyProgressState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No progress yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Take a practice test to start tracking your scores and accuracy by category.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AccuracySummary(stats: HomeStats) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AccuracyDonut(
            percent = stats.averageScorePercent ?: 0,
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            StatLine(label = "Tests Taken", value = stats.testsTaken.toString())
            StatLine(label = "Avg Score", value = "${stats.averageScorePercent ?: 0}%")
            StatLine(
                label = "Pass Rate",
                value = if (stats.recentTestsCount != null && stats.recentTestsCount > 0) {
                    "${stats.recentTestsPassed}/${stats.recentTestsCount}"
                } else {
                    "--"
                }
            )
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$value ",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AccuracyDonut(percent: Int, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val arcColor = if (percent >= PASS_THRESHOLD_PERCENT) CorrectGreen else MaterialTheme.colorScheme.primary
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.16f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = 360f * (percent / 100f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CategoryBarChart(breakdown: List<CategoryBreakdown>, onCategoryClick: (Category) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        breakdown.forEach { entry ->
            val barColor = if (entry.percent >= PASS_THRESHOLD_PERCENT) CorrectGreen else IncorrectRed
            val category = Category.entries.firstOrNull { it.displayName == entry.categoryName }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { base ->
                        if (category != null) base.clickable { onCategoryClick(category) } else base
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${entry.percent}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                    if (category != null) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Study ${entry.categoryName} flash cards",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(5.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(entry.percent / 100f)
                            .fillMaxHeight()
                            .background(color = barColor, shape = RoundedCornerShape(5.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreLineChart(history: List<ScorePoint>) {
    if (history.size < 2) {
        Text(
            text = "Take another practice test to see your score trend.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val passColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val leftPadding = 4.dp.toPx()
        val chartWidth = size.width - leftPadding * 2
        val chartHeight = size.height - 8.dp.toPx()

        fun yFor(percent: Int): Float = chartHeight - (chartHeight * (percent / 100f))

        // Gridlines at 0 / 25 / 50 / 75 / 100
        listOf(0, 25, 50, 75, 100).forEach { mark ->
            val y = yFor(mark)
            drawLine(
                color = if (mark == PASS_THRESHOLD_PERCENT) passColor else gridColor,
                start = Offset(leftPadding, y),
                end = Offset(leftPadding + chartWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = if (mark == PASS_THRESHOLD_PERCENT) {
                    PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                } else null
            )
        }

        val stepX = if (history.size > 1) chartWidth / (history.size - 1) else 0f
        val points = history.mapIndexed { index, point ->
            Offset(leftPadding + stepX * index, yFor(point.percent))
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        points.forEach { point ->
            drawCircle(color = lineColor, radius = 5.dp.toPx(), center = point)
        }
    }
}
