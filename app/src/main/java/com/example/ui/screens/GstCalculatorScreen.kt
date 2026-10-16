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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.GstMode
import com.example.ui.components.ClearableNumberField
import com.example.ui.components.ModeSegmentedRow
import com.example.ui.components.PresetChipRow
import com.example.ui.components.ResultDisplayCard
import com.example.viewmodel.CalculatorViewModel

@Composable
fun GstCalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val originalAmountInput by viewModel.gstOriginalAmount.collectAsState()
    val selectedRate by viewModel.gstSelectedRate.collectAsState()
    val customRateInput by viewModel.gstCustomRate.collectAsState()
    val isCustom by viewModel.isGstCustom.collectAsState()
    val mode by viewModel.gstMode.collectAsState()
    val currency by viewModel.activeCurrency.collectAsState()

    val amount = originalAmountInput.toDoubleOrNull() ?: 0.0
    val activeRate = if (isCustom) {
        customRateInput.toDoubleOrNull() ?: 0.0
    } else {
        selectedRate
    }

    val netAmount: Double
    val gstAmount: Double
    val totalAmount: Double

    if (mode == GstMode.ADD_TAX) {
        netAmount = amount
        gstAmount = amount * (activeRate / 100.0)
        totalAmount = amount + gstAmount
    } else {
        // Remove tax (Exclusive of tax)
        totalAmount = amount
        if (activeRate > 0) {
            netAmount = amount / (1.0 + (activeRate / 100.0))
            gstAmount = totalAmount - netAmount
        } else {
            netAmount = amount
            gstAmount = 0.0
        }
    }

    val halfGst = gstAmount / 2.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("gst_calculator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Segmented Buttons (Add Tax vs. Remove Tax)
        ModeSegmentedRow(
            items = listOf(GstMode.ADD_TAX, GstMode.REMOVE_TAX),
            selectedItem = mode,
            itemLabel = { if (it == GstMode.ADD_TAX) "Add Tax (+)" else "Remove Tax (-)" },
            onItemSelected = { viewModel.gstMode.value = it }
        )

        // Original Amount input
        ClearableNumberField(
            value = originalAmountInput,
            onValueChange = { viewModel.gstOriginalAmount.value = it },
            label = if (mode == GstMode.ADD_TAX) "Net / Base Amount" else "Gross / Total Bill Amount",
            prefix = currency.symbol,
            testTag = "input_gst_amount"
        )

        // Tax Rate presets (5%, 12%, 18%, 28%)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "GST / Tax Rate (%)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            PresetChipRow(
                options = listOf(5, 12, 18, 28),
                selectedOption = if (!isCustom) selectedRate.toInt() else null,
                onOptionSelected = {
                    viewModel.isGstCustom.value = false
                    viewModel.gstSelectedRate.value = it.toDouble()
                    viewModel.gstCustomRate.value = ""
                }
            )

            ClearableNumberField(
                value = customRateInput,
                onValueChange = {
                    viewModel.isGstCustom.value = true
                    viewModel.gstCustomRate.value = it
                },
                label = "Custom Tax Rate (%)",
                suffix = "%",
                testTag = "input_custom_tax_rate"
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Output Display Card
        ResultDisplayCard(
            title = if (mode == GstMode.ADD_TAX) "Total Payable Amount" else "Net Amount (Before Tax)",
            primaryValue = CalculatorViewModel.formatAmount(if (mode == GstMode.ADD_TAX) totalAmount else netAmount),
            currencySymbol = currency.symbol,
            breakdownItems = listOf(
                "Original Input" to "${currency.symbol} ${CalculatorViewModel.formatAmount(amount)}",
                "Tax Rate" to "${CalculatorViewModel.formatDecimals(activeRate, 2)}%",
                "Net Amount" to "${currency.symbol} ${CalculatorViewModel.formatAmount(netAmount)}",
                "GST / Tax Amount" to "${currency.symbol} ${CalculatorViewModel.formatAmount(gstAmount)}",
                "CGST (${CalculatorViewModel.formatDecimals(activeRate / 2.0, 1)}%)" to "${currency.symbol} ${CalculatorViewModel.formatAmount(halfGst)}",
                "SGST (${CalculatorViewModel.formatDecimals(activeRate / 2.0, 1)}%)" to "${currency.symbol} ${CalculatorViewModel.formatAmount(halfGst)}",
                "Total Payable" to "${currency.symbol} ${CalculatorViewModel.formatAmount(totalAmount)}"
            ),
            note = if (amount > 0) {
                if (mode == GstMode.ADD_TAX) {
                    "Includes ${currency.symbol}${CalculatorViewModel.formatAmount(gstAmount)} tax (${CalculatorViewModel.formatDecimals(activeRate, 1)}%)"
                } else {
                    "Extracted ${currency.symbol}${CalculatorViewModel.formatAmount(gstAmount)} tax from total ${currency.symbol}${CalculatorViewModel.formatAmount(totalAmount)}"
                }
            } else null,
            onSaveToHistory = if (amount > 0) {
                {
                    val modeLabel = if (mode == GstMode.ADD_TAX) "+GST" else "-GST"
                    viewModel.addHistoryItem(
                        category = "GST/Tax",
                        formula = "${currency.symbol}${CalculatorViewModel.formatAmount(amount)} $modeLabel ${CalculatorViewModel.formatDecimals(activeRate, 1)}%",
                        result = "${currency.symbol}${CalculatorViewModel.formatAmount(totalAmount)}"
                    )
                }
            } else null
        )
    }
}
