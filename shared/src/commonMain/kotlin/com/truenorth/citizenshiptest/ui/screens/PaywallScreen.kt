package com.truenorth.citizenshiptest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.truenorth.citizenshiptest.data.BillingProducts
import com.truenorth.citizenshiptest.data.ProductPrice

internal data class PricingPlan(
    val productId: String,
    val title: String,
    val price: String,
    val perMonth: String,
    val badge: String?
)

internal fun buildPlans(prices: Map<String, ProductPrice>): List<PricingPlan> {
    val oneMonth = prices[BillingProducts.ONE_MONTH]
    val threeMonths = prices[BillingProducts.THREE_MONTHS]
    return listOfNotNull(
        oneMonth?.let {
            PricingPlan(
                productId = BillingProducts.ONE_MONTH,
                title = "1-Month Pass",
                price = it.formattedPrice,
                perMonth = "One-time payment",
                badge = null
            )
        },
        threeMonths?.let {
            // formattedPrice is the only pre-localized string the store gives us -
            // the per-month approximation is computed from the raw amount instead
            // of hardcoded, so it can't drift from whatever's actually configured
            // in the store console, and shows the buyer's real charged currency
            // rather than an assumed one.
            val perMonthAmount = (it.priceMicros / 3.0) / 1_000_000.0
            PricingPlan(
                productId = BillingProducts.THREE_MONTHS,
                title = "3-Month Pass",
                price = it.formattedPrice,
                perMonth = "≈ ${formatAmount(perMonthAmount)} ${it.currencyCode} / month",
                badge = "MOST POPULAR"
            )
        }
    )
}

internal fun formatAmount(amount: Double): String {
    val cents = kotlin.math.round(amount * 100).toLong()
    val wholePart = cents / 100
    val centPart = (cents % 100).let { if (it < 0) -it else it }
    return "$wholePart.${centPart.toString().padStart(2, '0')}"
}

private data class ComparisonRow(
    val feature: String,
    val freeIncluded: Boolean,
    val freeNote: String? = null,
    val paidNote: String? = null
)

private val comparisonRows = listOf(
    ComparisonRow(feature = "Practice Tests", freeIncluded = true, freeNote = "Limited per day", paidNote = "Unlimited"),
    ComparisonRow(feature = "Flash Cards, all categories", freeIncluded = true),
    ComparisonRow(feature = "Progress tracking", freeIncluded = true),
    ComparisonRow(feature = "Test date countdown", freeIncluded = true),
    ComparisonRow(feature = "How the Real Test Works guide", freeIncluded = true),
    ComparisonRow(feature = "Report a question issue", freeIncluded = true),
    ComparisonRow(feature = "Favourite Questions", freeIncluded = false),
    ComparisonRow(feature = "Smart Review (practice your misses)", freeIncluded = false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    productPrices: Map<String, ProductPrice>,
    onSelectPlan: (String) -> Unit,
    onClose: () -> Unit
) {
    val plans = remember(productPrices) { buildPlans(productPrices) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PaywallHeader() }
            item { ComparisonTable() }
            if (plans.isEmpty()) {
                item { BillingUnavailableNotice() }
            } else {
                items(plans) { plan ->
                    PlanCard(
                        plan = plan,
                        onSelect = { onSelectPlan(plan.productId) }
                    )
                }
            }
            item { FinePrint() }
        }
    }
}

@Composable
private fun BillingUnavailableNotice() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Billing isn't available right now. Check your connection and try again shortly.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
    }
}

@Composable
private fun PaywallHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Filled.WorkspacePremium,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Unlock Full Access",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "You've used today's free practice test. Come back tomorrow for another, or get unlimited access with a prepaid pass.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ComparisonTable() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "", modifier = Modifier.weight(2f))
                Text(
                    text = "Free",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Paid",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            comparisonRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.feature,
                        modifier = Modifier.weight(2f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ComparisonCell(included = row.freeIncluded, note = row.freeNote)
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        ComparisonCell(included = true, note = row.paidNote)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonCell(included: Boolean, note: String?) {
    if (note != null) {
        Text(
            text = note,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = if (included) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else if (included) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = "Included",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(20.dp)
        )
    } else {
        Text(
            text = "—",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlanCard(plan: PricingPlan, onSelect: () -> Unit) {
    val isFeatured = plan.badge != null
    val borderColor = if (isFeatured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = if (isFeatured) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (plan.badge != null) {
                Text(
                    text = plan.badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = plan.perMonth,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = plan.price,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onSelect, modifier = Modifier.fillMaxWidth()) {
                Text("Choose ${plan.title}")
            }
        }
    }
}

@Composable
private fun FinePrint() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = "One-time payment. Passes do not auto-renew — no surprise charges.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
    }
}
