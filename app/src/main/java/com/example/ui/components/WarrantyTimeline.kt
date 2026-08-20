package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ReceiptItem
import com.example.domain.model.WarrantyStatus
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiringSoon

@Composable
fun WarrantyTimeline(
    receipt: ReceiptItem,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = receipt.progressFraction,
        label = "warranty_progress"
    )
    val isDark = isSystemInDarkTheme()

    val progressColor = when (receipt.warrantyStatus) {
        WarrantyStatus.ACTIVE -> StatusActive
        WarrantyStatus.EXPIRING_SOON -> StatusExpiringSoon
        WarrantyStatus.EXPIRED -> StatusExpired
        WarrantyStatus.NO_WARRANTY -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("warranty_timeline_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.3f else 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 1.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(progressColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Warranty Status",
                            tint = progressColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Warranty Lifecycle",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (receipt.remainingDays > 0) "${receipt.remainingDays} days remaining" else "Coverage ended",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = progressColor
                        )
                    }
                }

                WarrantyBadge(
                    status = receipt.warrantyStatus,
                    remainingDays = receipt.remainingDays
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    BrandPrimary,
                                    progressColor
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dates & Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PURCHASED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = receipt.formattedPurchaseDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TOTAL PLAN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${receipt.warrantyMonths} Mo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EXPIRES ON",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = receipt.formattedExpiryDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
            }
        }
    }
}

