package com.example.csbecountdown.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Updated shapes to follow Material 3 guidelines
val Shapes = Shapes(
    small = RoundedCornerShape(12.dp),   // Buttons, chips, small components
    medium = RoundedCornerShape(16.dp),  // Cards, dialogs, medium components
    large = RoundedCornerShape(24.dp)    // Bottom sheets, expanded components
)