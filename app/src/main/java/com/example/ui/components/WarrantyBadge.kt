package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.WarrantyStatus
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiringSoon
import com.example.ui.theme.StatusNoWarranty

@Composable
fun WarrantyBadge(
    status: WarrantyStatus,
    remainingDays: Long,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, label) = when (status) {
        WarrantyStatus.ACTIVE -> {
            val daysStr = if (remainingDays > 365) {
                "${remainingDays / 365}y ${(remainingDays % 365) / 30}m left"
            } else if (remainingDays > 30) {
                "${remainingDays / 30}m ${remainingDays % 30}d left"
            } else {
                "$remainingDays days left"
            }
            Triple(StatusActive.copy(alpha = 0.15f), StatusActive, daysStr)
        }
        WarrantyStatus.EXPIRING_SOON -> {
            val text = when {
                remainingDays <= 0 -> "Expires today!"
                remainingDays == 1L -> "1 day left!"
                else -> "$remainingDays days left"
            }
            Triple(StatusExpiringSoon.copy(alpha = 0.20f), StatusExpiringSoon, text)
        }
        WarrantyStatus.EXPIRED -> {
            Triple(StatusExpired.copy(alpha = 0.15f), StatusExpired, "Expired")
        }
        WarrantyStatus.NO_WARRANTY -> {
            Triple(StatusNoWarranty.copy(alpha = 0.15f), StatusNoWarranty, "No Warranty")
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("warranty_badge_${status.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
