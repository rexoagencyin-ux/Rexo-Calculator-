package com.example.model

enum class CalculatorTab(val label: String) {
    PERCENTAGE("Percentage"),
    GST_TAX("Bill & GST"),
    WEIGHT_PRICE("Weight"),
    VOLUME_PRICE("Volume"),
    DISCOUNT("Discount")
}

data class CurrencyItem(
    val code: String,
    val symbol: String,
    val name: String
)

val SupportedCurrencies = listOf(
    CurrencyItem("INR", "₹", "Indian Rupee"),
    CurrencyItem("USD", "$", "US Dollar"),
    CurrencyItem("EUR", "€", "Euro"),
    CurrencyItem("GBP", "£", "British Pound"),
    CurrencyItem("JPY", "¥", "Japanese Yen"),
    CurrencyItem("AED", "د.إ", "UAE Dirham"),
    CurrencyItem("CAD", "C$", "Canadian Dollar"),
    CurrencyItem("AUD", "A$", "Australian Dollar")
)

enum class WeightCalcMode {
    PRICE_BY_WEIGHT,
    QUANTITY_BY_BUDGET
}

enum class WeightUnit(val label: String, val multiplierToKg: Double) {
    KILOGRAM("kg", 1.0),
    GRAM("g", 0.001)
}

enum class VolumeCalcMode {
    PRICE_BY_VOLUME,
    VOLUME_BY_BUDGET
}

enum class VolumeUnit(val label: String, val multiplierToLiter: Double) {
    LITER("L", 1.0),
    MILLILITER("ml", 0.001)
}

enum class GstMode {
    ADD_TAX,
    REMOVE_TAX
}

enum class PercentageCalcType(val title: String) {
    PERCENT_OF_TOTAL("Percentage of Total"),
    PERCENT_INCREASE_DECREASE("Percentage Change (±%)"),
    DISCOUNT_PERCENT("Quick Discount Rate")
}

data class CalculationRecord(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val category: String,
    val formula: String,
    val result: String
)
