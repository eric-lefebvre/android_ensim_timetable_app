package com.example.ensim_app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ensim_app.ui.theme.Gold
import com.example.ensim_app.ui.theme.Navy800
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue
import com.example.ensim_app.util.mondayOfLocal
import com.example.ensim_app.util.startOfDayMs
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal const val INITIAL_PAGE = Int.MAX_VALUE / 2

private fun localMonthName(month: Int): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris")).apply {
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
        .replaceFirstChar { it.uppercase() }
}

private fun localDayInitials(): List<String> {
    val symbols = DateFormatSymbols(Locale.getDefault())
    // shortWeekdays: index 1=Sun, 2=Mon … 7=Sat — we want Mon(2)…Sun(1)
    return listOf(2, 3, 4, 5, 6, 7, 1).map { idx ->
        symbols.shortWeekdays[idx].firstOrNull()?.uppercase() ?: ""
    }
}

internal data class YearMonth(val year: Int, val month: Int) {
    fun shifted(delta: Int): YearMonth {
        val total = year * 12 + month + delta
        return YearMonth(Math.floorDiv(total, 12), Math.floorMod(total, 12))
    }
}

@Composable
fun WeekPickerDialog(
    initialSelectedDate: Date,
    onConfirm: (Date) -> Unit,
    onDismiss: () -> Unit,
) {
    PickerDialog(
        initialSelectedDate = initialSelectedDate,
        onDismiss = onDismiss,
        confirmLabel = "OK",
        buildHighlight = { date -> WeekHighlight(mondayOfLocal(date).time) },
        onConfirm = onConfirm,
    )
}

@Composable
fun DayPickerDialog(
    initialSelectedDate: Date,
    onConfirm: (Date) -> Unit,
    onDismiss: () -> Unit,
) {
    PickerDialog(
        initialSelectedDate = initialSelectedDate,
        onDismiss = onDismiss,
        confirmLabel = "OK",
        buildHighlight = { date -> DayHighlight(startOfDayMs(date)) },
        onConfirm = onConfirm,
    )
}

private sealed interface Highlight {
    fun matches(cellDate: Date, col: Int): Boolean
    fun isFirst(cellDate: Date, col: Int): Boolean
    fun isLast(cellDate: Date, col: Int): Boolean
}

private data class WeekHighlight(val mondayMs: Long) : Highlight {
    override fun matches(cellDate: Date, col: Int): Boolean =
        mondayOfLocal(cellDate).time == mondayMs && col in 0..4
    override fun isFirst(cellDate: Date, col: Int): Boolean = matches(cellDate, col) && col == 0
    override fun isLast(cellDate: Date, col: Int): Boolean = matches(cellDate, col) && col == 4
}

private data class DayHighlight(val dayMs: Long) : Highlight {
    override fun matches(cellDate: Date, col: Int): Boolean = startOfDayMs(cellDate) == dayMs
    override fun isFirst(cellDate: Date, col: Int): Boolean = matches(cellDate, col)
    override fun isLast(cellDate: Date, col: Int): Boolean = matches(cellDate, col)
}

@Composable
private fun PickerDialog(
    initialSelectedDate: Date,
    onDismiss: () -> Unit,
    confirmLabel: String,
    buildHighlight: (Date) -> Highlight,
    onConfirm: (Date) -> Unit,
) {
    val initialMonth = remember(initialSelectedDate) {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris")).apply { time = initialSelectedDate }
        YearMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }
    val monthPagerState = rememberPagerState(initialPage = INITIAL_PAGE) { Int.MAX_VALUE }
    val pickerScope = rememberCoroutineScope()
    val visibleMonth = remember(monthPagerState.currentPage, initialMonth) {
        initialMonth.shifted(monthPagerState.currentPage - INITIAL_PAGE)
    }
    var selectedDate by remember { mutableStateOf(initialSelectedDate) }
    val highlight = remember(selectedDate) { buildHighlight(selectedDate) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Navy800) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${localMonthName(visibleMonth.month)} ${visibleMonth.year}",
                        color = OffWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = {
                        pickerScope.launch {
                            monthPagerState.animateScrollToPage(monthPagerState.currentPage - 1)
                        }
                    }) {
                        Icon(Icons.Default.KeyboardArrowLeft, stringResource(R.string.cal_prev_month_cd), tint = OffWhite)
                    }
                    IconButton(onClick = {
                        pickerScope.launch {
                            monthPagerState.animateScrollToPage(monthPagerState.currentPage + 1)
                        }
                    }) {
                        Icon(Icons.Default.KeyboardArrowRight, stringResource(R.string.cal_next_month_cd), tint = OffWhite)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    localDayInitials().forEach { label ->
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(label, color = SlateBlue, fontSize = 11.sp)
                        }
                    }
                }
                HorizontalPager(state = monthPagerState) { page ->
                    val pageMonth = initialMonth.shifted(page - INITIAL_PAGE)
                    MonthGrid(
                        yearMonth = pageMonth,
                        highlight = highlight,
                        onPickDay = { selectedDate = it },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cal_cancel), color = OffWhite) }
                    TextButton(onClick = { onConfirm(selectedDate) }) {
                        Text(stringResource(R.string.cal_ok), color = Gold, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    highlight: Highlight,
    onPickDay: (Date) -> Unit,
) {
    val cal = remember(yearMonth) {
        Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris")).apply {
            clear()
            set(yearMonth.year, yearMonth.month, 1)
        }
    }
    val firstColOffset = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val rowCount = ((firstColOffset + daysInMonth + 6) / 7)

    Column {
        for (row in 0 until rowCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayOfMonth = cellIndex - firstColOffset + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val dayCal = remember(yearMonth, dayOfMonth) {
                            Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris")).apply {
                                clear()
                                set(yearMonth.year, yearMonth.month, dayOfMonth)
                            }
                        }
                        val dayDate = dayCal.time
                        val highlighted = highlight.matches(dayDate, col)
                        DayCell(
                            day = dayOfMonth,
                            highlighted = highlighted,
                            isFirstHighlight = highlight.isFirst(dayDate, col),
                            isLastHighlight = highlight.isLast(dayDate, col),
                            onClick = { onPickDay(dayDate) },
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    highlighted: Boolean,
    isFirstHighlight: Boolean,
    isLastHighlight: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = when {
        isFirstHighlight && isLastHighlight -> RoundedCornerShape(50)
        isFirstHighlight -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
        isLastHighlight -> RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
        else -> RoundedCornerShape(0)
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (highlighted) Gold.copy(alpha = 0.35f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = day.toString(), color = OffWhite, fontSize = 14.sp)
    }
}

