package com.example.ensim_app.ui.screen.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ensim_app.domain.Resource
import com.example.ensim_app.service.model.Cours
import com.example.ensim_app.ui.component.INITIAL_PAGE
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.buildCourseColors
import com.example.ensim_app.util.mondayToFridayOfWeek
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun CalendarScreen(
    coursResource: Resource<List<Cours>>?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (coursResource) {
            null -> Text(
                text = stringResource(R.string.calendar_no_schedule),
                color = OffWhite,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
            )
            is Resource.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
            is Resource.Error -> Text(
                stringResource(R.string.calendar_loading_error),
                color = OffWhite,
                modifier = Modifier.align(Alignment.Center),
            )
            is Resource.Success -> WeekScreen(cours = coursResource.data)
        }
    }
}

@Composable
private fun WeekScreen(cours: List<Cours>) {
    val today = remember { Date() }
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGE) { Int.MAX_VALUE }
    val scope = rememberCoroutineScope()

    val colorMap = remember(cours) { buildCourseColors(cours.map { it.matiere }) }

    var selectedCours by remember { mutableStateOf<Cours?>(null) }

    val currentWeekDates = remember(pagerState.currentPage, today) {
        mondayToFridayOfWeek(today, pagerState.currentPage - INITIAL_PAGE)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WeekSelectorPill(
            weekDates = currentWeekDates,
            today = today,
            onPickOffset = { offset ->
                scope.launch { pagerState.animateScrollToPage(INITIAL_PAGE + offset) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            val dates = remember(page, today) {
                mondayToFridayOfWeek(today, page - INITIAL_PAGE)
            }
            val weekCours = remember(dates, cours) {
                val weekStart = dates.first().time
                val weekEnd = dates.last().time + DAY_MS - 1
                cours.filter { it.debut.time in weekStart..weekEnd }
            }
            WeekGrid(
                weekDates = dates,
                weekCours = weekCours,
                colorMap = colorMap,
                onCoursClick = { selectedCours = it },
            )
        }
    }

    selectedCours?.let { cours ->
        CoursDetailsDialog(
            cours = cours,
            color = colorMap.getValue(cours.matiere),
            onDismiss = { selectedCours = null },
        )
    }
}
