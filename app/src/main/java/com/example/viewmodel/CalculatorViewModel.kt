package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.CalculationRecord
import com.example.model.CalculatorTab
import com.example.model.CurrencyItem
import com.example.model.GstMode
import com.example.model.PercentageCalcType
import com.example.model.SupportedCurrencies
import com.example.model.VolumeCalcMode
import com.example.model.VolumeUnit
import com.example.model.WeightCalcMode
import com.example.model.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class CalculatorViewModel : ViewModel() {

    // Global navigation & settings
    private val _activeTab = MutableStateFlow(CalculatorTab.PERCENTAGE)
    val activeTab: StateFlow<CalculatorTab> = _activeTab.asStateFlow()

    private val _activeCurrency = MutableStateFlow(SupportedCurrencies[0]) // Default INR
    val activeCurrency: StateFlow<CurrencyItem> = _activeCurrency.asStateFlow()

    private val _history = MutableStateFlow<List<CalculationRecord>>(emptyList())
    val history: StateFlow<List<CalculationRecord>> = _history.asStateFlow()

    private val _showHistorySheet = MutableStateFlow(false)
    val showHistorySheet: StateFlow<Boolean> = _showHistorySheet.asStateFlow()

    private val _showPrivacySheet = MutableStateFlow(false)
    val showPrivacySheet: StateFlow<Boolean> = _showPrivacySheet.asStateFlow()

    private val _showCurrencyDialog = MutableStateFlow(false)
    val showCurrencyDialog: StateFlow<Boolean> = _showCurrencyDialog.asStateFlow()

    // 1. Percentage Calculator State
    val percentType = MutableStateFlow(PercentageCalcType.PERCENT_OF_TOTAL)
    val percentValueA = MutableStateFlow("")
    val percentValueB = MutableStateFlow("")

    // 2. GST Calculator State
    val gstOriginalAmount = MutableStateFlow("")
    val gstSelectedRate = MutableStateFlow(18.0)
    val gstCustomRate = MutableStateFlow("")
    val isGstCustom = MutableStateFlow(false)
    val gstMode = MutableStateFlow(GstMode.ADD_TAX)

    // 3. Weight Calculator State
    val weightRatePerKg = MutableStateFlow("")
    val weightMode = MutableStateFlow(WeightCalcMode.PRICE_BY_WEIGHT)
    val weightInput = MutableStateFlow("")
    val weightUnit = MutableStateFlow(WeightUnit.GRAM)
    val weightBudgetInput = MutableStateFlow("")

    // 4. Volume Calculator State
    val volumeRatePerLiter = MutableStateFlow("")
    val volumeMode = MutableStateFlow(VolumeCalcMode.PRICE_BY_VOLUME)
    val volumeInput = MutableStateFlow("")
    val volumeUnit = MutableStateFlow(VolumeUnit.MILLILITER)
    val volumeBudgetInput = MutableStateFlow("")

    // 5. Discount Calculator State
    val discountOriginalPrice = MutableStateFlow("")
    val discountPercentage = MutableStateFlow("")
    val discountCouponPercent = MutableStateFlow("")

    // Navigation and Modals
    fun selectTab(tab: CalculatorTab) {
        _activeTab.value = tab
    }

    fun setCurrency(currency: CurrencyItem) {
        _activeCurrency.value = currency
    }

    fun setHistorySheetVisible(visible: Boolean) {
        _showHistorySheet.value = visible
    }

    fun setPrivacySheetVisible(visible: Boolean) {
        _showPrivacySheet.value = visible
    }

    fun setCurrencyDialogVisible(visible: Boolean) {
        _showCurrencyDialog.value = visible
    }

    // History management
    fun addHistoryItem(category: String, formula: String, result: String) {
        val newRecord = CalculationRecord(
            category = category,
            formula = formula,
            result = result
        )
        _history.value = listOf(newRecord) + _history.value.take(49)
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    // Number formatting helper
    companion object {
        fun formatAmount(value: Double): String {
            if (value.isNaN() || value.isInfinite()) return "0.00"
            return String.format(Locale.US, "%,.2f", value)
        }

        fun formatDecimals(value: Double, places: Int = 2): String {
            if (value.isNaN() || value.isInfinite()) return "0"
            val pattern = "%,.${places}f"
            return String.format(Locale.US, pattern, value).trimEnd('0').trimEnd('.')
        }
    }
}
