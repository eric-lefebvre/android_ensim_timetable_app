package com.example.ensim_app.ui.screen.calendar

import androidx.compose.ui.unit.dp

internal const val START_HOUR = 7
internal const val END_HOUR = 20
internal const val HOUR_SLOTS = END_HOUR - START_HOUR
internal val TimeAxisWidth = 36.dp
internal const val DAY_MS = 24L * 60 * 60 * 1000
internal const val GRID_TOTAL_MIN = (END_HOUR - START_HOUR) * 60
internal const val GRID_START_MIN = START_HOUR * 60
