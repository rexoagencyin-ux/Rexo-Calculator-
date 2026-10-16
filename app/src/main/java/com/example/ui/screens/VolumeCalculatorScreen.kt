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
import androidx.compose.ui.unit.dp
import com.example.model.VolumeCalcMode
import com.example.model.VolumeUnit
import com.example.ui.components.ClearableNumberField
import com.example.ui.components.ModeSegmentedRow
import com.example.ui.components.PresetChipRow
import com.example.ui.components.ResultDisplayCard
import com.example.viewmodel.CalculatorViewModel

@Composable
fun VolumeCalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val rateInput by viewModel.volumeRatePerLiter.collectAsState()
    val mode by viewModel.volumeMode.collectAsState()
    val volumeInput by viewModel.volumeInput.collectAsState()
    val volumeUnit by viewModel.volumeUnit.collectAsState()
    val budgetInput by viewModel.volumeBudgetInput.collectAsState()
    val currency by viewModel.activeCurrency.collectAsState()

    val rate = rateInput.toDoubleOrNull() ?: 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("volume_calculator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector: Find Price vs Find Volume
        ModeSegmentedRow(
            items = listOf(VolumeCalcMode.PRICE_BY_VOLUME, VolumeCalcMode.VOLUME_BY_BUDGET),
            selectedItem = mode,
            itemLabel = { if (it == VolumeCalcMode.PRICE_BY_VOLUME) "Price by Volume" else "Volume by Budget" },
            onItemSelected = { viewModel.volumeMode.value = it }
        )

        // Base rate input (per Liter)
        ClearableNumberField(
            value = rateInput,
            onValueChange = { viewModel.volumeRatePerLiter.value = it },
            label = "Base Rate (per 1 Liter)",
            prefix = currency.symbol,
            suffix = "/ L",
            testTag = "input_volume_base_rate"
        )

        if (mode == VolumeCalcMode.PRICE_BY_VOLUME) {
            // Desired Volume input
            ClearableNumberField(
                value = volumeInput,
                onValueChange = { viewModel.volumeInput.value = it },
                label = "Desired Volume",
                suffix = volumeUnit.label,
                testTag = "input_desired_volume"
            )

            // Unit toggle (Milliliters vs Liters)
            ModeSegmentedRow(
                items = listOf(VolumeUnit.MILLILITER, VolumeUnit.LITER),
                selectedItem = volumeUnit,
                itemLabel = { if (it == VolumeUnit.MILLILITER) "Milliliters (ml)" else "Liters (L)" },
                onItemSelected = { viewModel.volumeUnit.value = it }
            )

            // Quick volume presets
            val presets = if (volumeUnit == VolumeUnit.MILLILITER) {
                listOf(100, 250, 500, 750)
            } else {
                listOf(1, 2, 5, 10)
            }
            PresetChipRow(
                options = presets,
                selectedOption = volumeInput.toIntOrNull(),
                onOptionSelected = { viewModel.volumeInput.value = it.toString() },
                suffix = volumeUnit.label
            )

            val enteredVolume = volumeInput.toDoubleOrNull() ?: 0.0
            val volumeInLiters = enteredVolume * volumeUnit.multiplierToLiter
            val calculatedPrice = rate * volumeInLiters

            val mlStr = if (volumeUnit == VolumeUnit.MILLILITER) "$enteredVolume ml" else "${CalculatorViewModel.formatDecimals(enteredVolume * 1000.0, 0)} ml"
            val lStr = if (volumeUnit == VolumeUnit.LITER) "$enteredVolume L" else "${CalculatorViewModel.formatDecimals(volumeInLiters, 3)} L"

            Spacer(modifier = Modifier.height(4.dp))

            ResultDisplayCard(
                title = "Calculated Price",
                primaryValue = CalculatorViewModel.formatAmount(calculatedPrice),
                currencySymbol = currency.symbol,
                breakdownItems = listOf(
                    "Base Rate" to "${currency.symbol} ${CalculatorViewModel.formatAmount(rate)} / L",
                    "Entered Volume" to "$enteredVolume ${volumeUnit.label}",
                    "In Milliliters" to mlStr,
                    "In Liters" to lStr,
                    "Total Price" to "${currency.symbol} ${CalculatorViewModel.formatAmount(calculatedPrice)}"
                ),
                note = if (rate > 0 && enteredVolume > 0) {
                    "$mlStr ($lStr) @ ${currency.symbol}$rate/L"
                } else null,
                onSaveToHistory = if (rate > 0 && enteredVolume > 0) {
                    {
                        viewModel.addHistoryItem(
                            category = "Volume",
                            formula = "$enteredVolume${volumeUnit.label} @ ${currency.symbol}$rate/L",
                            result = "${currency.symbol}${CalculatorViewModel.formatAmount(calculatedPrice)}"
                        )
                    }
                } else null
            )
        } else {
            // Find Volume by Budget
            ClearableNumberField(
                value = budgetInput,
                onValueChange = { viewModel.volumeBudgetInput.value = it },
                label = "Budget Amount",
                prefix = currency.symbol,
                testTag = "input_volume_budget"
            )

            // Quick budget presets
            PresetChipRow(
                options = listOf(10, 20, 50, 100, 200, 500),
                selectedOption = budgetInput.toIntOrNull(),
                onOptionSelected = { viewModel.volumeBudgetInput.value = it.toString() },
                suffix = ""
            )

            val budget = budgetInput.toDoubleOrNull() ?: 0.0
            val totalLiters = if (rate > 0) budget / rate else 0.0
            val totalMl = totalLiters * 1000.0

            val primaryDisplay = if (totalMl >= 1000.0) {
                "${CalculatorViewModel.formatDecimals(totalLiters, 3)} L"
            } else {
                "${CalculatorViewModel.formatDecimals(totalMl, 1)} ml"
            }

            Spacer(modifier = Modifier.height(4.dp))

            ResultDisplayCard(
                title = "Calculated Volume",
                primaryValue = primaryDisplay,
                currencySymbol = "",
                breakdownItems = listOf(
                    "Budget Amount" to "${currency.symbol} ${CalculatorViewModel.formatAmount(budget)}",
                    "Base Rate" to "${currency.symbol} ${CalculatorViewModel.formatAmount(rate)} / L",
                    "Exact Milliliters" to "${CalculatorViewModel.formatDecimals(totalMl, 2)} ml",
                    "Exact Liters" to "${CalculatorViewModel.formatDecimals(totalLiters, 3)} L"
                ),
                note = if (rate > 0 && budget > 0) {
                    "For ${currency.symbol}$budget you get $primaryDisplay"
                } else null,
                onSaveToHistory = if (rate > 0 && budget > 0) {
                    {
                        viewModel.addHistoryItem(
                            category = "Volume",
                            formula = "${currency.symbol}$budget budget @ ${currency.symbol}$rate/L",
                            result = primaryDisplay
                        )
                    }
                } else null
            )
        }
    }
}
