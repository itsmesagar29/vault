package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ReceiptCategory
import com.example.ui.theme.BrandPrimary

@Composable
fun CategoryChips(
    selectedCategory: ReceiptCategory?,
    onCategorySelected: (ReceiptCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" Chip
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = {
                Text(
                    text = "All",
                    fontSize = 12.sp,
                    fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Medium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "All",
                    modifier = Modifier.size(16.dp)
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BrandPrimary,
                selectedLabelColor = Color.White,
                selectedLeadingIconColor = Color.White,
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurface
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selectedCategory == null,
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.3f else 0.5f),
                selectedBorderColor = BrandPrimary,
                borderWidth = 1.dp,
                selectedBorderWidth = 1.dp
            ),
            modifier = Modifier.testTag("category_chip_all")
        )

        // Categories
        ReceiptCategory.entries.forEach { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.displayName,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandPrimary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.3f else 0.5f),
                    selectedBorderColor = BrandPrimary,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                ),
                modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
            )
        }
    }
}

