package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.ReceiptCategory
import com.example.domain.model.ReceiptItem

@Entity(
    tableName = "receipts",
    indices = [
        Index(value = ["warrantyExpiryDateMillis"]),
        Index(value = ["merchantName"]),
        Index(value = ["category"])
    ]
)
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchantName: String,
    val itemName: String,
    val totalAmount: Double,
    val currency: String,
    val purchaseDateMillis: Long,
    val warrantyMonths: Int,
    val warrantyExpiryDateMillis: Long,
    val category: String,
    val notes: String,
    val imagePath: String?,
    val rawOcrText: String,
    val confidenceScore: Float,
    val reminderEnabled: Boolean,
    val reminderDaysBefore: Int,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): ReceiptItem {
        return ReceiptItem(
            id = id,
            merchantName = merchantName,
            itemName = itemName,
            totalAmount = totalAmount,
            currency = currency,
            purchaseDateMillis = purchaseDateMillis,
            warrantyMonths = warrantyMonths,
            warrantyExpiryDateMillis = warrantyExpiryDateMillis,
            category = ReceiptCategory.fromString(category),
            notes = notes,
            imagePath = imagePath,
            rawOcrText = rawOcrText,
            confidenceScore = confidenceScore,
            reminderEnabled = reminderEnabled,
            reminderDaysBefore = reminderDaysBefore,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(item: ReceiptItem): ReceiptEntity {
            return ReceiptEntity(
                id = item.id,
                merchantName = item.merchantName,
                itemName = item.itemName,
                totalAmount = item.totalAmount,
                currency = item.currency,
                purchaseDateMillis = item.purchaseDateMillis,
                warrantyMonths = item.warrantyMonths,
                warrantyExpiryDateMillis = item.warrantyExpiryDateMillis,
                category = item.category.name,
                notes = item.notes,
                imagePath = item.imagePath,
                rawOcrText = item.rawOcrText,
                confidenceScore = item.confidenceScore,
                reminderEnabled = item.reminderEnabled,
                reminderDaysBefore = item.reminderDaysBefore,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt
            )
        }
    }
}
