package com.truenorth.citizenshiptest.ui.screens

import com.truenorth.citizenshiptest.data.BillingProducts
import com.truenorth.citizenshiptest.data.ProductPrice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaywallScreenTest {

    @Test
    fun formatAmount_roundsToNearestCent_ratherThanTruncating() {
        // 14.99 / 3 = 4.99666... - must round up to 5.00, not truncate down to 4.99.
        assertEquals("5.00", formatAmount(4.996666666))
        assertEquals("4.99", formatAmount(4.994444444))
    }

    @Test
    fun formatAmount_padsSingleDigitCents() {
        assertEquals("4.05", formatAmount(4.05))
        assertEquals("4.00", formatAmount(4.0))
    }

    @Test
    fun buildPlans_withNoPrices_returnsEmptyList() {
        assertEquals(emptyList(), buildPlans(emptyMap()))
    }

    @Test
    fun buildPlans_withOnlyOneMonthPrice_returnsJustThatPlan() {
        val prices = mapOf(
            BillingProducts.ONE_MONTH to ProductPrice(
                productId = BillingProducts.ONE_MONTH,
                formattedPrice = "$7.99",
                priceMicros = 7_990_000,
                currencyCode = "CAD"
            )
        )

        val plans = buildPlans(prices)

        assertEquals(1, plans.size)
        assertEquals(BillingProducts.ONE_MONTH, plans[0].productId)
        assertEquals("$7.99", plans[0].price)
        assertEquals("One-time payment", plans[0].perMonth)
        assertEquals(null, plans[0].badge)
    }

    @Test
    fun buildPlans_withThreeMonthPrice_computesPerMonthFromRawMicros_notHardcoded() {
        val prices = mapOf(
            BillingProducts.THREE_MONTHS to ProductPrice(
                productId = BillingProducts.THREE_MONTHS,
                formattedPrice = "$14.99",
                priceMicros = 14_990_000,
                currencyCode = "CAD"
            )
        )

        val plans = buildPlans(prices)

        assertEquals(1, plans.size)
        assertEquals("MOST POPULAR", plans[0].badge)
        assertTrue(plans[0].perMonth.contains("5.00"), "expected rounded per-month amount, got: ${plans[0].perMonth}")
        assertTrue(plans[0].perMonth.contains("CAD"))
    }

    @Test
    fun buildPlans_withBothPrices_ordersOneMonthBeforeThreeMonth() {
        val prices = mapOf(
            BillingProducts.ONE_MONTH to ProductPrice(BillingProducts.ONE_MONTH, "$7.99", 7_990_000, "CAD"),
            BillingProducts.THREE_MONTHS to ProductPrice(BillingProducts.THREE_MONTHS, "$14.99", 14_990_000, "CAD")
        )

        val plans = buildPlans(prices)

        assertEquals(listOf(BillingProducts.ONE_MONTH, BillingProducts.THREE_MONTHS), plans.map { it.productId })
    }
}
