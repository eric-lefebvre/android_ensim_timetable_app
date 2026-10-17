package com.example.ensim_app.ui.screen.calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ensim_app.service.model.Cours
import com.example.ensim_app.ui.theme.Navy900
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue
import com.example.ensim_app.util.isSameDay
import com.example.ensim_app.util.minutesOfDay
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun WeekGrid(
    weekDates: List<Date>,
    weekCours: List<Cours>,
    colorMap: Map<String, Color>,
    onCoursClick: (Cours) -> Unit,
) {
    val dayFmt = remember { SimpleDateFormat("dd", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("Europe/Paris") } }
    val gridColor = Navy900.copy(alpha = 0.15f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite),
    ) {
        WeekDayHeaderRow(weekDates = weekDates, dayFmt = dayFmt)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            TimeAxisColumn()
            for (i in 0 until 5) {
                DayColumn(
                    dayDate = weekDates[i],
                    weekCours = weekCours,
                    colorMap = colorMap,
                    gridColor = gridColor,
                    onCoursClick = onCoursClick,
                )
            }
        }
    }
}

@Composable
private fun WeekDayHeaderRow(weekDates: List<Date>, dayFmt: SimpleDateFormat) {
    val dayLabels = remember {
        val symbols = DateFormatSymbols(Locale.getDefault())
        (2..6).map { idx -> symbols.shortWeekdays[idx].replaceFirstChar { it.uppercase() } }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Spacer(modifier = Modifier.width(TimeAxisWidth))
        dayLabels.forEachIndexed { i, label ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = label,
                    color = Navy900,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = dayFmt.format(weekDates[i]),
                    color = Navy900,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun RowScope.TimeAxisColumn() {
    Column(
        modifier = Modifier
            .width(TimeAxisWidth)
            .fillMaxHeight(),
    ) {
        for (slot in 0 until HOUR_SLOTS) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                if (slot > 0) {
                    Text(
                        text = "${START_HOUR + slot}h",
                        color = SlateBlue,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(y = (-7).dp)
                            .padding(end = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayColumn(
    dayDate: Date,
    weekCours: List<Cours>,
    colorMap: Map<String, Color>,
    gridColor: Color,
    onCoursClick: (Cours) -> Unit,
) {
    val dayCours = remember(weekCours, dayDate) {
        val filtered = weekCours
            .filter { isSameDay(it.debut, dayDate) }
            .sortedBy { it.debut }
        filtered.zipWithNext().forEach { (a, b) ->
            if (a.fin.after(b.debut)) {
                Log.e(
                    "ENSIM",
                    "Overlap détecté: ${a.matiere} (${a.debut}-${a.fin}) vs ${b.matiere} (${b.debut}-${b.fin})",
                )
            }
        }
        filtered
    }
    BoxWithConstraints(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
    ) {
        val columnHeight = maxHeight
        Column(modifier = Modifier.fillMaxSize()) {
            for (slot in 0 until HOUR_SLOTS) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .drawBehind {
                            val stroke = 1.dp.toPx()
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = stroke,
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = stroke,
                            )
                        },
                )
            }
        }
        dayCours.forEach { c ->
            val bounds = eventBoundsDp(c, columnHeight)
            if (bounds.height > 0.dp) {
                EventBlock(
                    cours = c,
                    color = colorMap.getValue(c.matiere),
                    onClick = { onCoursClick(c) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = bounds.top)
                        .fillMaxWidth()
                        .height(bounds.height),
                )
            }
        }
    }
}

private data class EventBounds(val top: Dp, val height: Dp)

private fun eventBoundsDp(cours: Cours, columnHeight: Dp): EventBounds {
    val startMin = minutesOfDay(cours.debut)
    val endMin = minutesOfDay(cours.fin)
    val topFraction = ((startMin - GRID_START_MIN).coerceAtLeast(0).toFloat() / GRID_TOTAL_MIN)
    val bottomFraction = ((endMin - GRID_START_MIN).coerceAtMost(GRID_TOTAL_MIN).toFloat() / GRID_TOTAL_MIN)
    return EventBounds(
        top = columnHeight * topFraction,
        height = columnHeight * (bottomFraction - topFraction),
    )
}
