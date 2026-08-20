package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.VaultStats
import com.example.domain.model.WarrantyStatus
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusExpiringSoon
import com.example.ui.theme.VaultHeroGradientDark
import com.example.ui.theme.VaultHeroGradientLight
import java.util.Locale

@Composable
fun StatSummaryCards(
    stats: VaultStats,
    selectedStatusFilter: WarrantyStatus?,
    onStatusFilterClick: (WarrantyStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Column(modifier = modifier.fillMaxWidth()) {
        // Vault Total Value Hero Card (High Polish Fintech Card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 8.dp else 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = BrandPrimary.copy(alpha = 0.3f),
                    spotColor = BrandPrimary.copy(alpha = 0.4f)
                )
                .testTag("vault_hero_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = if (isDark) VaultHeroGradientDark else VaultHeroGradientLight,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (isDark) 0.15f else 0.25f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = BrandAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PROTECTED ASSET VAULT",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        // Live Protection Indicator Pill
                        Surface(
                            shape = CircleShape,
                            color = StatusActive.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusActive.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(StatusActive)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ENCRYPTED ON-DEVICE",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${stats.currency}${String.format(Locale.US, "%,.2f", stats.totalVaultValue)}",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${stats.totalReceiptsCount} Total Records  •  ${stats.activeWarrantiesCount} Under Active Warranty",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Three Interactive Status Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatFilterCard(
                title = "Active",
                count = stats.activeWarrantiesCount,
                icon = Icons.Default.Shield,
                accentColor = StatusActive,
                isSelected = selectedStatusFilter == WarrantyStatus.ACTIVE,
                onClick = {
                    if (selectedStatusFilter == WarrantyStatus.ACTIVE) {
                        onStatusFilterClick(null)
                    } else {
                        onStatusFilterClick(WarrantyStatus.ACTIVE)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            StatFilterCard(
                title = "Expiring",
                count = stats.expiringSoonCount,
                icon = Icons.Default.NotificationImportant,
                accentColor = StatusExpiringSoon,
                isSelected = selectedStatusFilter == WarrantyStatus.EXPIRING_SOON,
                onClick = {
                    if (selectedStatusFilter == WarrantyStatus.EXPIRING_SOON) {
                        onStatusFilterClick(null)
                    } else {
                        onStatusFilterClick(WarrantyStatus.EXPIRING_SOON)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            StatFilterCard(
                title = "Expired",
                count = stats.expiredCount,
                icon = Icons.Default.HourglassBottom,
                accentColor = StatusExpired,
                isSelected = selectedStatusFilter == WarrantyStatus.EXPIRED,
                onClick = {
                    if (selectedStatusFilter == WarrantyStatus.EXPIRED) {
                        onStatusFilterClick(null)
                    } else {
                        onStatusFilterClick(WarrantyStatus.EXPIRED)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatFilterCard(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 200),
        label = "stat_border"
    )

    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) {
            accentColor.copy(alpha = if (isDark) 0.2f else 0.12f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 200),
        label = "stat_bg"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("filter_stat_${title.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        color = animatedBgColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = animatedBorderColor
        ),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

