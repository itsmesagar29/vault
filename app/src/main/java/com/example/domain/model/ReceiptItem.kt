package com.example.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ReceiptItem(
    val id: Long = 0,
    val merchantName: String,
    val itemName: String,
    val totalAmount: Double,
    val currency: String = "$",
    val purchaseDateMillis: Long,
    val warrantyMonths: Int,
    val warrantyExpiryDateMillis: Long,
    val category: ReceiptCategory,
    val notes: String = "",
    val imagePath: String? = null,
    val rawOcrText: String = "",
    val confidenceScore: Float = 1.0f,
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 7,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val warrantyStatus: WarrantyStatus
        get() = WarrantyStatus.calculate(warrantyExpiryDateMillis)

    val remainingDays: Long
        get() {
            val now = System.currentTimeMillis()
            val diff = warrantyExpiryDateMillis - now
            return TimeUnit.MILLISECONDS.toDays(diff)
        }

    val formattedPurchaseDate: String
        get() = formatDate(purchaseDateMillis)

    val formattedExpiryDate: String
        get() = formatDate(warrantyExpiryDateMillis)

    val formattedAmount: String
        get() = "$currency${String.format(Locale.US, "%.2f", totalAmount)}"

    val progressFraction: Float
        get() {
            val totalDuration = warrantyExpiryDateMillis - purchaseDateMillis
            if (totalDuration <= 0) return 1.0f
            val elapsed = System.currentTimeMillis() - purchaseDateMillis
            return (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0.0f, 1.0f)
        }

    companion object {
        fun formatDate(millis: Long): String {
            if (millis <= 0) return "N/A"
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }
}
