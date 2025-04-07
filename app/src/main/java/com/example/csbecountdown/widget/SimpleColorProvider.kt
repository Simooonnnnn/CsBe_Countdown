package com.example.csbecountdown.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

/**
 * Creates a ColorProvider from a single Color
 */
fun colorProvider(color: Color): ColorProvider {
    return ColorProvider(color)
}

/**
 * Creates a ColorProvider from a resource ID
 */
fun colorProviderResource(resId: Int): ColorProvider {
    return ColorProvider(resId)
}