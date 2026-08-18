package com.example.domain.model

data class ParsedReceipt(
    val merchantName: String,
    val itemName: String,
    val totalAmount: Double?,
    val currency: String = "$",
    val purchaseDateMillis: Long,
    val warrantyMonths: Int,
    val warrantyExpiryDateMillis: Long,
    val category: ReceiptCategory,
    val rawText: String,
    val overallConfidence: Float, // 0.0f to 1.0f
    val fieldConfidence: Map<String, Float> = emptyMap(),
    val extractedLines: List<String> = emptyList()
)
