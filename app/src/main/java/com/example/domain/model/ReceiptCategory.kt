package com.example.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.ui.graphics.vector.ImageVector

enum class ReceiptCategory(
    val displayName: String,
    val defaultWarrantyMonths: Int,
    val icon: ImageVector
) {
    ELECTRONICS("Electronics", 12, Icons.Filled.Power),
    APPLIANCES("Appliances", 24, Icons.Filled.Kitchen),
    VEHICLES("Vehicles & Auto", 36, Icons.Filled.DirectionsCar),
    FURNITURE("Furniture", 12, Icons.Filled.Weekend),
    CLOTHING("Clothing & Shoes", 3, Icons.Filled.Checkroom),
    TOOLS("Tools & Hardware", 24, Icons.Filled.Build),
    OTHER("Other", 12, Icons.Filled.MoreHoriz);

    companion object {
        fun fromString(value: String?): ReceiptCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: OTHER
        }

        fun inferFromText(text: String): ReceiptCategory {
            val lower = text.lowercase()
            return when {
                lower.contains("apple") || lower.contains("best buy") || lower.contains("dell") ||
                        lower.contains("sony") || lower.contains("samsung") || lower.contains("laptop") ||
                        lower.contains("phone") || lower.contains("tv") || lower.contains("headphone") ||
                        lower.contains("computer") || lower.contains("electronics") -> ELECTRONICS

                lower.contains("washer") || lower.contains("dryer") || lower.contains("refrigerator") ||
                        lower.contains("fridge") || lower.contains("microwave") || lower.contains("oven") ||
                        lower.contains("dishwasher") || lower.contains("vacuum") || lower.contains("dyson") -> APPLIANCES

                lower.contains("auto") || lower.contains("toyota") || lower.contains("honda") ||
                        lower.contains("ford") || lower.contains("tire") || lower.contains("oil") ||
                        lower.contains("autozone") || lower.contains("battery") -> VEHICLES

                lower.contains("ikea") || lower.contains("sofa") || lower.contains("table") ||
                        lower.contains("chair") || lower.contains("desk") || lower.contains("mattress") ||
                        lower.contains("furniture") || lower.contains("wayfair") -> FURNITURE

                lower.contains("nike") || lower.contains("zara") || lower.contains("h&m") ||
                        lower.contains("shoes") || lower.contains("jacket") || lower.contains("shirt") ||
                        lower.contains("clothing") || lower.contains("apparel") -> CLOTHING

                lower.contains("home depot") || lower.contains("lowe") || lower.contains("dewalt") ||
                        lower.contains("milwaukee") || lower.contains("drill") || lower.contains("tool") ||
                        lower.contains("hardware") -> TOOLS

                else -> OTHER
            }
        }
    }
}
