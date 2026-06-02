package com.sevam.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object SevamColors {
    val Orange = Color(0xFFFF7A1A)
    val OrangeContainer = Color(0xFFFFF1E6)
    val Navy = Color(0xFF173A77)
    val NavyMuted = Color(0xFF5F79A6)
    val Success = Color(0xFF12B76A)
    val SuccessContainer = Color(0xFFE8FFF3)
    val Warning = Color(0xFFF79009)
    val SurfaceAlt = Color(0xFFF7F8FC)
    val Border = Color(0xFFE6EAF2)
    val TextSecondary = Color(0xFF667085)
}

private val LightColors = lightColorScheme(
    primary = SevamColors.Orange,
    onPrimary = Color.White,
    secondary = SevamColors.Navy,
    onSecondary = Color.White,
    tertiary = SevamColors.Success,
    background = Color(0xFFF7F8FB),
    surface = Color.White,
    onSurface = Color(0xFF101828),
    surfaceVariant = Color(0xFFF1F4F8),
    outline = SevamColors.Border,
)

private val DarkColors = darkColorScheme(
    primary = SevamColors.Orange,
    secondary = Color(0xFF9EBAEA),
    tertiary = SevamColors.Success,
)

private val SevamTypography = Typography(
    headlineMedium = TextStyle(
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium,
    ),
)

@Composable
fun SevamTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SevamTypography,
        content = content,
    )
}
