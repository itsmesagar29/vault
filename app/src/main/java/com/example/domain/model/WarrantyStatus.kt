package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiringSoon
import com.example.ui.theme.StatusNoWarranty

enum class WarrantyStatus(
    val title: String,
    val color: Color
) {
    ACTIVE("Active", StatusActive),
    EXPIRING_SOON("Expiring Soon", StatusExpiringSoon),
    EXPIRED("Expired", StatusExpired),
    NO_WARRANTY("No Warranty", StatusNoWarranty);

    companion object {
        fun calculate(expiryEpochMillis: Long, currentTimeMillis: Long = System.currentTimeMillis()): WarrantyStatus {
            if (expiryEpochMillis <= 0) return NO_WARRANTY
            val diffDays = (expiryEpochMillis - currentTimeMillis) / (1000 * 60 * 60 * 24)
            return when {
                diffDays < 0 -> EXPIRED
                diffDays <= 30 -> EXPIRING_SOON
                else -> ACTIVE
            }
        }
    }
}
