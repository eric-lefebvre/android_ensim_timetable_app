package com.example.ensim_app.ui.theme

import androidx.compose.ui.graphics.Color

private const val GOLDEN_RATIO = 0.61803398875f
private const val SATURATION = 0.55f
private const val LIGHTNESS = 0.72f

fun buildCourseColors(matieres: Collection<String>): Map<String, Color> {
    val sorted = matieres.distinct().sorted()
    return sorted.mapIndexed { i, m ->
        var hue = (i * GOLDEN_RATIO * 360f) % 360f
        if (hue < 0f) hue += 360f
        m to Color.hsl(hue, SATURATION, LIGHTNESS)
    }.toMap()
}
