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
import com.example.model.PercentageCalcType
import com.example.ui.components.ClearableNumberField
import com.example.ui.components.ModeSegmentedRow
import com.example.ui.components.PresetChipRow
import com.example.ui.components.ResultDisplayCard
import com.example.viewmodel.CalculatorViewModel

@Composable
fun PercentageCalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val currentType by viewModel.percentType.collectAsState()
    val valueA by viewModel.percentValueA.collectAsState()
    val valueB by viewModel.percentValueB.collectAsState()
    val currency by viewModel.activeCurrency.collectAsState()

    val numA = valueA.toDoubleOrNull() ?: 0.0
    val numB = valueB.toDoubleOrNull() ?: 0.0

    // Calculations based on mode
    val resultText: String
    val breakdown: List<Pair<String, String>>
    val note: String?
    val formula: String

    when (currentType) {
        PercentageCalcType.PERCENT_OF_TOTAL -> {
            // What is A% of B?
            val calculated = (numA / 100.0) * numB
            resultText = CalculatorViewModel.formatAmount(calculated)
            breakdown = listOf(
                "Percentage" to "$numA%",
                "Base Amount" to "${currency.symbol} ${CalculatorViewModel.formatAmount(numB)}",
                "Remaining Value" to "${currency.symbol} ${CalculatorViewModel.formatAmount(numB - calculated)}"
            )
            note = if (numA > 0 && numB > 0) "$numA% of ${currency.symbol}$numB equals $resultText" else null
            formula = "$numA% of ${currency.symbol}$numB"
        }

        PercentageCalcType.PERCENT_INCREASE_DECREASE -> {
            // From A to B
            if (numA > 0.0 && valueB.isNotEmpty()) {
                val diff = numB - numA
                val pctChange = (diff / numA) * 100.0
                val sign = if (pctChange >= 0) "+" else ""
                resultText = "$sign${CalculatorViewModel.formatDecimals(pctChange, 2)}%"
                breakdown = listOf(
                    "Initial Value" to "${currency.symbol} ${CalculatorViewModel.formatAmount(numA)}",
                    "Final Value" to "${currency.symbol} ${CalculatorViewModel.formatAmount(numB)}",
                    "Absolute Difference" to "${if (diff >= 0) "+" else ""}${currency.symbol} ${CalculatorViewModel.formatAmount(diff)}"
                )
                note = if (pctChange >= 0) "Growth of ${CalculatorViewModel.formatDecimals(pctChange, 2)}%" else "Decline of ${CalculatorViewModel.formatDecimals(-pctChange, 2)}%"
                formula = "${currency.symbol}$numA to ${currency.symbol}$numB"
            } else {
                resultText = "0.00%"
                breakdown = emptyList()
                note = "Enter both initial and new values"
                formula = ""
            }
        }

        PercentageCalcType.DISCOUNT_PERCENT -> {
            // Original Price A, Discounted Price B -> % discount
            if (numA > 0.0 && numB >= 0.0 && numB <= numA) {
                val saved = numA - numB
                val discountPct = (saved / numA) * 100.0
                resultText = "${CalculatorViewModel.formatDecimals(discountPct, 2)}% OFF"
                breakdown = listOf(
                    "Original Price" to "${currency.symbol} ${CalculatorViewModel.formatAmount(numA)}",
                    "Final Discounted Price" to "${currency.symbol} ${CalculatorViewModel.formatAmount(numB)}",
                    "Total Discount Savings" to "${currency.symbol} ${CalculatorViewModel.formatAmount(saved)}"
                )
                note = "You save ${currency.symbol}${CalculatorViewModel.formatAmount(saved)} ($resultText)"
                formula = "${currency.symbol}$numA down to ${currency.symbol}$numB"
            } else {
                resultText = "0.00% OFF"
                breakdown = emptyList()
                note = if (numB > numA && numA > 0) "Discounted price cannot exceed original price" else "Enter original and discounted price"
                formula = ""
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("percentage_calculator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode selector
        ModeSegmentedRow(
            items = PercentageCalcType.entries,
            selectedItem = currentType,
            itemLabel = { type ->
                when (type) {
                    PercentageCalcType.PERCENT_OF_TOTAL -> "% of Total"
                    PercentageCalcType.PERCENT_INCREASE_DECREASE -> "±% Change"
                    PercentageCalcType.DISCOUNT_PERCENT -> "% Off"
                }
            },
            onItemSelected = { viewModel.percentType.value = it }
        )

        // Inputs based on mode
        when (currentType) {
            PercentageCalcType.PERCENT_OF_TOTAL -> {
                ClearableNumberField(
                    value = valueA,
                    onValueChange = { viewModel.percentValueA.value = it },
                    label = "Percentage (%)",
                    suffix = "%",
                    testTag = "input_percent_rate"
                )

                // Quick preset percentage chips
                PresetChipRow(
                    options = listOf(5, 10, 15, 18, 20, 25, 50),
                    selectedOption = valueA.toIntOrNull(),
                    onOptionSelected = { viewModel.percentValueA.value = it.toString() }
                )

                ClearableNumberField(
                    value = valueB,
                    onValueChange = { viewModel.percentValueB.value = it },
                    label = "Total / Base Amount",
                    prefix = currency.symbol,
                    testTag = "input_total_amount"
                )
            }

            PercentageCalcType.PERCENT_INCREASE_DECREASE -> {
                ClearableNumberField(
                    value = valueA,
                    onValueChange = { viewModel.percentValueA.value = it },
                    label = "Initial Value",
                    prefix = currency.symbol,
                    testTag = "input_initial_value"
                )

                ClearableNumberField(
                    value = valueB,
                    onValueChange = { viewModel.percentValueB.value = it },
                    label = "New / Final Value",
                    prefix = currency.symbol,
                    testTag = "input_new_value"
                )
            }

            PercentageCalcType.DISCOUNT_PERCENT -> {
                ClearableNumberField(
                    value = valueA,
                    onValueChange = { viewModel.percentValueA.value = it },
                    label = "Original Price",
                    prefix = currency.symbol,
                    testTag = "input_original_price"
                )

                ClearableNumberField(
                    value = valueB,
                    onValueChange = { viewModel.percentValueB.value = it },
                    label = "Final Sale Price",
                    prefix = currency.symbol,
                    testTag = "input_sale_price"
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Output Result Card
        ResultDisplayCard(
            title = when (currentType) {
                PercentageCalcType.PERCENT_OF_TOTAL -> "Calculated Percentage Value"
                PercentageCalcType.PERCENT_INCREASE_DECREASE -> "Percentage Change"
                PercentageCalcType.DISCOUNT_PERCENT -> "Calculated Discount Rate"
            },
            primaryValue = resultText,
            currencySymbol = if (currentType == PercentageCalcType.PERCENT_OF_TOTAL) currency.symbol else "",
            breakdownItems = breakdown,
            note = note,
            onSaveToHistory = if (formula.isNotEmpty()) {
                {
                    viewModel.addHistoryItem(
                        category = "Percentage",
                        formula = formula,
                        result = if (currentType == PercentageCalcType.PERCENT_OF_TOTAL) "${currency.symbol}$resultText" else resultText
                    )
                }
            } else null
        )
    }
}
