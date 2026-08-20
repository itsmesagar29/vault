package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand Color Palette tokens (Modern Fintech / Security Vault)
val BrandPrimary = Color(0xFF2563EB)         // Royal Electric Blue
val BrandPrimaryDark = Color(0xFF1D4ED8)
val BrandPrimaryLight = Color(0xFF60A5FA)
val BrandSecondary = Color(0xFF64748B)       // Slate
val BrandAccent = Color(0xFF06B6D4)          // Cyan
val BrandAccentDark = Color(0xFF0891B2)
val BrandIndigo = Color(0xFF4F46E5)          // Deep Indigo
val BrandPurple = Color(0xFF7C3AED)          // Vibrant Violet

// Dark Theme Surfaces (Deep Space / Cyber Slate)
val DarkBackground = Color(0xFF0B0F19)       // Deep Obsidian
val DarkSurface = Color(0xFF131B2E)          // Elevated Card Surface
val DarkSurfaceVariant = Color(0xFF1E293B)   // Surface borders/chips
val DarkSurfaceBorder = Color(0xFF2E3D5B)    // Subtle card border
val DarkTextPrimary = Color(0xFFF8FAFC)      // Crisp High-Contrast Text
val DarkTextSecondary = Color(0xFF94A3B8)    // Muted Slate Text

// Light Theme Surfaces (Clean Porcelain / Snow Slate)
val LightBackground = Color(0xFFF6F8FC)      // Soft Off-White Clean Slate
val LightSurface = Color(0xFFFFFFFF)         // Pure White Card
val LightSurfaceVariant = Color(0xFFEDF2F7)  // Light Divider/Chip
val LightSurfaceBorder = Color(0xFFE2E8F0)   // Crisp Border
val LightTextPrimary = Color(0xFF0F172A)     // Dark Slate Navy Text
val LightTextSecondary = Color(0xFF64748B)   // Slate Muted Text

// Status Indicators
val StatusActive = Color(0xFF10B981)         // Emerald Green
val StatusActiveContainer = Color(0xFFD1FAE5)// Light Green
val StatusExpiringSoon = Color(0xFFF59E0B)   // Amber Gold
val StatusExpiringSoonContainer = Color(0xFFFEF3C7)
val StatusExpired = Color(0xFFEF4444)        // Coral Red
val StatusExpiredContainer = Color(0xFFFEE2E2)
val StatusNoWarranty = Color(0xFF64748B)     // Slate

// Gradients
val VaultHeroGradientDark = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E3A8A),
        Color(0xFF1E1B4B),
        Color(0xFF0B0F19)
    )
)

val VaultHeroGradientLight = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1D4ED8),
        Color(0xFF2563EB),
        Color(0xFF3B82F6)
    )
)

val AccentGlowGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2563EB),
        Color(0xFF06B6D4),
        Color(0xFF7C3AED)
    )
)

val ExpiringCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF78350F).copy(alpha = 0.25f),
        Color(0xFF451A03).copy(alpha = 0.15f)
    )
)

