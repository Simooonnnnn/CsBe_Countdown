package com.example.csbecountdown.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider

/**
 * Creates a ColorProvider from a single Color
 */
fun colorProvider(color: Color): ColorProvider {
    return ColorProvider(color, color)
}

/**
 * Creates a ColorProvider with different day/night colors
 */
fun colorProvider(dayColor: Color, nightColor: Color): ColorProvider {
    return ColorProvider(dayColor, nightColor)
}