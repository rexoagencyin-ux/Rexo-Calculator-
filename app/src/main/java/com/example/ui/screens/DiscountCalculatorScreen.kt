package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.ClearableNumberField
import com.example.ui.components.PresetChipRow
import com.example.ui.components.ResultDisplayCard
import com.example.viewmodel.CalculatorViewModel

@Composable
fun DiscountCalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val originalPriceInput by viewModel.discountOriginalPrice.collectAsState()
    val discountPercentInput by viewModel.discountPercentage.collectAsState()
    val couponPercentInput by viewModel.discountCouponPercent.collectAsState()
    val currency by viewModel.activeCurrency.collectAsState()

    val originalPrice = originalPriceInput.toDoubleOrNull() ?: 0.0
    val discountPct = discountPercentInput.toDoubleOrNull() ?: 0.0
    val couponPct = couponPercentInput.toDoubleOrNull() ?: 0.0

    // Sequential discounts (Standard retail practice: base discount, then extra coupon applied on discounted price)
    val primarySavings = originalPrice * (discountPct / 100.0)
    val priceAfterPrimary = (originalPrice - primarySavings).coerceAtLeast(0.0)

    val couponSavings = priceAfterPrimary * (couponPct / 100.0)
    val finalPrice = (priceAfterPrimary - couponSavings).coerceAtLeast(0.0)

    val totalSavings = primarySavings + couponSavings
    val effectivePct = if (originalPrice > 0) (totalSavings / originalPrice) * 100.0 else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("discount_calculator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Original Price
        ClearableNumberField(
            value = originalPriceInput,
            onValueChange = { viewModel.discountOriginalPrice.value = it },
            label = "Original Price",
            prefix = currency.symbol,
            testTag = "input_discount_original_price"
        )

        // Discount Percentage
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ClearableNumberField(
                value = discountPercentInput,
                onValueChange = { viewModel.discountPercentage.value = it },
                label = "Discount Percentage (%)",
                suffix = "%",
                testTag = "input_discount_rate"
            )

            // Preset discount chips
            PresetChipRow(
                options = listOf(5, 10, 15, 20, 25, 30, 40, 50),
                selectedOption = discountPercentInput.toIntOrNull(),
                onOptionSelected = { viewModel.discountPercentage.value = it.toString() }
            )
        }

        // Additional coupon/discount
        ClearableNumberField(
            value = couponPercentInput,
            onValueChange = { viewModel.discountCouponPercent.value = it },
            label = "Additional Coupon Discount (Optional %)",
            suffix = "%",
            testTag = "input_coupon_discount"
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Output Display Card
        ResultDisplayCard(
            title = "Final Price After Discount",
            primaryValue = CalculatorViewModel.formatAmount(finalPrice),
            currencySymbol = currency.symbol,
            breakdownItems = listOf(
                "Original Price" to "${currency.symbol} ${CalculatorViewModel.formatAmount(originalPrice)}",
                "Primary Discount ($discountPct%)" to "- ${currency.symbol} ${CalculatorViewModel.formatAmount(primarySavings)}",
                "Coupon Discount ($couponPct%)" to if (couponPct > 0) "- ${currency.symbol} ${CalculatorViewModel.formatAmount(couponSavings)}" else "None",
                "Total Savings" to "${currency.symbol} ${CalculatorViewModel.formatAmount(totalSavings)}",
                "Effective Discount" to "${CalculatorViewModel.formatDecimals(effectivePct, 2)}% OFF",
                "You Pay" to "${currency.symbol} ${CalculatorViewModel.formatAmount(finalPrice)}"
            ),
            note = if (originalPrice > 0 && totalSavings > 0) {
                "You save a total of ${currency.symbol}${CalculatorViewModel.formatAmount(totalSavings)} (${CalculatorViewModel.formatDecimals(effectivePct, 1)}% OFF)"
            } else null,
            onSaveToHistory = if (originalPrice > 0 && discountPct > 0) {
                {
                    val couponStr = if (couponPct > 0) " + $couponPct% coupon" else ""
                    viewModel.addHistoryItem(
                        category = "Discount",
                        formula = "${currency.symbol}${CalculatorViewModel.formatAmount(originalPrice)} - $discountPct%$couponStr",
                        result = "${currency.symbol}${CalculatorViewModel.formatAmount(finalPrice)}"
                    )
                }
            } else null
        )
    }
}
