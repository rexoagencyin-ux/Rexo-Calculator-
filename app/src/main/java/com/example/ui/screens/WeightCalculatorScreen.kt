package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.model.WeightCalcMode
import com.example.model.WeightUnit
import com.example.ui.components.ClearableNumberField
import com.example.ui.components.ModeSegmentedRow
import com.example.ui.components.PresetChipRow
import com.example.ui.components.ResultDisplayCard
import com.example.viewmodel.CalculatorViewModel

@Composable
fun WeightCalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val rateInput by viewModel.weightRatePerKg.collectAsState()
    val mode by viewModel.weightMode.collectAsState()
    val weightInput by viewModel.weightInput.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()
    val budgetInput by viewModel.weightBudgetInput.collectAsState()
    val currency by viewModel.activeCurrency.collectAsState()

    val rate = rateInput.toDoubleOrNull() ?: 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("weight_calculator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector: Find Price vs Find Quantity
        ModeSegmentedRow(
            items = listOf(WeightCalcMode.PRICE_BY_WEIGHT, WeightCalcMode.QUANTITY_BY_BUDGET),
            selectedItem = mode,
            itemLabel = { if (it == WeightCalcMode.PRICE_BY_WEIGHT) "Price by Weight" else "Weight by Budget" },
            onItemSelected = { viewModel.weightMode.value = it }
        )

        // Base rate input (per kg)
        ClearableNumberField(
            value = rateInput,
            onValueChange = { viewModel.weightRatePerKg.value = it },
            label = "Base Rate (per 1 kg)",
            prefix = currency.symbol,
            suffix = "/ kg",
            testTag = "input_weight_base_rate"
        )

        if (mode == WeightCalcMode.PRICE_BY_WEIGHT) {
            // Desired Weight input
            ClearableNumberField(
                value = weightInput,
                onValueChange = { viewModel.weightInput.value = it },
                label = "Desired Weight",
                suffix = weightUnit.label,
                testTag = "input_desired_weight"
            )

            // Unit toggle (Grams vs Kilograms)
            ModeSegmentedRow(
                items = listOf(WeightUnit.GRAM, WeightUnit.KILOGRAM),
                selectedItem = weightUnit,
                itemLabel = { if (it == WeightUnit.GRAM) "Grams (g)" else "Kilograms (kg)" },
                onItemSelected = { viewModel.weightUnit.value = it }
            )

            // Quick weight presets
            val presets = if (weightUnit == WeightUnit.GRAM) {
                listOf(100, 250, 500, 750)
            } else {
                listOf(1, 2, 5, 10)
            }
            PresetChipRow(
                options = presets,
                selectedOption = weightInput.toIntOrNull(),
                onOptionSelected = { viewModel.weightInput.value = it.toString() },
                suffix = weightUnit.label
            )

            val enteredWeight = weightInput.toDoubleOrNull() ?: 0.0
            val weightInKg = enteredWeight * weightUnit.multiplierToKg
            val calculatedPrice = rate * weightInKg

            val gramsStr = if (weightUnit == WeightUnit.GRAM) "$enteredWeight g" else "${CalculatorViewModel.formatDecimals(enteredWeight * 1000.0, 0)} g"
            val kgStr = if (weightUnit == WeightUnit.KILOGRAM) "$enteredWeight kg" else "${CalculatorViewModel.formatDecimals(weightInKg, 3)} kg"

            Spacer(modifier = Modifier.height(4.dp))

            ResultDisplayCard(
                title = "Calculated Price",
                primaryValue = CalculatorViewModel.formatAmount(calculatedPrice),
                currencySymbol = currency.symbol,
                breakdownItems = listOf(
                    "Base Rate" to "${currency.symbol} ${CalculatorViewModel.formatAmount(rate)} / kg",
                    "Entered Quantity" to "$enteredWeight ${weightUnit.label}",
                    "In Grams" to gramsStr,
                    "In Kilograms" to kgStr,
                    "Total Price" to "${currency.symbol} ${CalculatorViewModel.formatAmount(calculatedPrice)}"
                ),
                note = if (rate > 0 && enteredWeight > 0) {
                    "$gramsStr ($kgStr) @ ${currency.symbol}$rate/kg"
                } else null,
                onSaveToHistory = if (rate > 0 && enteredWeight > 0) {
                    {
                        viewModel.addHistoryItem(
                            category = "Weight",
                            formula = "$enteredWeight${weightUnit.label} @ ${currency.symbol}$rate/kg",
                            result = "${currency.symbol}${CalculatorViewModel.formatAmount(calculatedPrice)}"
                        )
                    }
                } else null
            )
        } else {
            // Find Quantity by Budget
            ClearableNumberField(
                value = budgetInput,
                onValueChange = { viewModel.weightBudgetInput.value = it },
                label = "Budget Amount",
                prefix = currency.symbol,
                testTag = "input_weight_budget"
            )

            // Quick budget presets
            PresetChipRow(
                options = listOf(10, 20, 50, 100, 200, 500),
                selectedOption = budgetInput.toIntOrNull(),
                onOptionSelected = { viewModel.weightBudgetInput.value = it.toString() },
                suffix = ""
            )

            val budget = budgetInput.toDoubleOrNull() ?: 0.0
            val totalKg = if (rate > 0) budget / rate else 0.0
            val totalGrams = totalKg * 1000.0

            val primaryDisplay = if (totalGrams >= 1000.0) {
                "${CalculatorViewModel.formatDecimals(totalKg, 3)} kg"
            } else {
                "${CalculatorViewModel.formatDecimals(totalGrams, 1)} g"
            }

            Spacer(modifier = Modifier.height(4.dp))

            ResultDisplayCard(
                title = "Calculated Quantity",
                primaryValue = primaryDisplay,
                currencySymbol = "",
                breakdownItems = listOf(
                    "Budget Amount" to "${currency.symbol} ${CalculatorViewModel.formatAmount(budget)}",
                    "Base Rate" to "${currency.symbol} ${CalculatorViewModel.formatAmount(rate)} / kg",
                    "Exact Grams" to "${CalculatorViewModel.formatDecimals(totalGrams, 2)} g",
                    "Exact Kilograms" to "${CalculatorViewModel.formatDecimals(totalKg, 3)} kg"
                ),
                note = if (rate > 0 && budget > 0) {
                    "For ${currency.symbol}$budget you get $primaryDisplay"
                } else null,
                onSaveToHistory = if (rate > 0 && budget > 0) {
                    {
                        viewModel.addHistoryItem(
                            category = "Weight",
                            formula = "${currency.symbol}$budget budget @ ${currency.symbol}$rate/kg",
                            result = primaryDisplay
                        )
                    }
                } else null
            )
        }
    }
}
