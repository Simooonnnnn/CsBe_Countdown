package com.example.csbecountdown.widget

import android.content.Context
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.csbecountdown.ui.theme.md_theme_dark_background
import com.example.csbecountdown.ui.theme.md_theme_dark_onPrimaryContainer
import com.example.csbecountdown.ui.theme.md_theme_dark_onSurface
import com.example.csbecountdown.ui.theme.md_theme_dark_onSurfaceVariant
import com.example.csbecountdown.ui.theme.md_theme_dark_primaryContainer
import com.example.csbecountdown.ui.theme.md_theme_dark_surface
import com.example.csbecountdown.ui.theme.md_theme_light_background
import com.example.csbecountdown.ui.theme.md_theme_light_onPrimaryContainer
import com.example.csbecountdown.ui.theme.md_theme_light_onSurface
import com.example.csbecountdown.ui.theme.md_theme_light_onSurfaceVariant
import com.example.csbecountdown.ui.theme.md_theme_light_primaryContainer
import com.example.csbecountdown.ui.theme.md_theme_light_surface

/**
 * Simple color helper for Material You integration
 */
object DynamicColorHelper {

    // Get dynamic color or fallback to predefined theme colors based on dark mode
    fun getColorForMode(
        context: Context,
        lightColor: Color,
        darkColor: Color,
        colorResourceId: Int,
        isDarkTheme: Boolean
    ): Color {
        // On Android 12+, try to use Material You dynamic colors
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                // Try to get the color from resources
                val dynamicColor = ContextCompat.getColor(context, colorResourceId)
                Color(dynamicColor)
            } catch (e: Exception) {
                // Fallback to predefined colors if dynamic colors are not available
                if (isDarkTheme) darkColor else lightColor
            }
        } else {
            // For older Android versions, use predefined colors
            if (isDarkTheme) darkColor else lightColor
        }
    }

    // Helper function to check if we're in dark mode
    fun isDarkTheme(context: Context): Boolean {
        return context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    // Get surface color
    fun getSurface(context: Context): Color {
        val isDark = isDarkTheme(context)
        return getColorForMode(
            context,
            md_theme_light_surface,
            md_theme_dark_surface,
            android.R.color.system_accent1_50,
            isDark
        )
    }

    // Get onSurface color
    fun getOnSurface(context: Context): Color {
        val isDark = isDarkTheme(context)
        return getColorForMode(
            context,
            md_theme_light_onSurface,
            md_theme_dark_onSurface,
            android.R.color.system_accent1_900,
            isDark
        )
    }

    // Get onSurfaceVariant color
    fun getOnSurfaceVariant(context: Context): Color {
        val isDark = isDarkTheme(context)
        return getColorForMode(
            context,
            md_theme_light_onSurfaceVariant,
            md_theme_dark_onSurfaceVariant,
            android.R.color.system_accent1_700,
            isDark
        )
    }

    // Get primaryContainer color
    fun getPrimaryContainer(context: Context): Color {
        val isDark = isDarkTheme(context)
        return getColorForMode(
            context,
            md_theme_light_primaryContainer,
            md_theme_dark_primaryContainer,
            android.R.color.system_accent1_100,
            isDark
        )
    }

    // Get onPrimaryContainer color
    fun getOnPrimaryContainer(context: Context): Color {
        val isDark = isDarkTheme(context)
        return getColorForMode(
            context,
            md_theme_light_onPrimaryContainer,
            md_theme_dark_onPrimaryContainer,
            android.R.color.system_accent1_900,
            isDark
        )
    }
}