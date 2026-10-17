package com.example.ensim_app.ui.screen.search

import android.util.Log
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ensim_app.domain.Resource
import com.example.ensim_app.service.PlanningSource
import com.example.ensim_app.service.RoomScheduleRepository
import com.example.ensim_app.service.Software
import com.example.ensim_app.service.model.Cours
import com.example.ensim_app.ui.component.DayPickerDialog
import com.example.ensim_app.ui.theme.Gold
import com.example.ensim_app.ui.theme.Navy800
import com.example.ensim_app.ui.theme.Navy900
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class RoomAvailability(
    val name: String,
    val isFree: Boolean,
    val currentEvent: Cours?,
)

private sealed interface TimeMode {
    data object Now : TimeMode
    data class Custom(val date: Date, val hour: Int, val minute: Int) : TimeMode
}

private val TimeModeSaver: Saver<TimeMode, Any> = Saver(
    save = { mode ->
        when (mode) {
            TimeMode.Now -> listOf(false, 0L, 0, 0)
            is TimeMode.Custom -> listOf(true, mode.date.time, mode.hour, mode.minute)
        }
    },
    restore = { saved ->
        @Suppress("UNCHECKED_CAST")
        val list = saved as List<Any>
        if (list[0] as Boolean) {
            TimeMode.Custom(
                date = Date(list[1] as Long),
                hour = list[2] as Int,
                minute = list[3] as Int,
            )
        } else TimeMode.Now
    },
)

private val SoftwareSetSaver: Saver<Set<Software>, Any> = Saver(
    save = { it.map(Software::name) },
    restore = { saved ->
        @Suppress("UNCHECKED_CAST")
        (saved as List<String>).map(Software::valueOf).toSet()
    },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    var timeMode by rememberSaveable(stateSaver = TimeModeSaver) {
        mutableStateOf<TimeMode>(TimeMode.Now)
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var requireComputers by rememberSaveable { mutableStateOf(false) }
    var softwareFilters by rememberSaveable(stateSaver = SoftwareSetSaver) {
        mutableStateOf(emptySet<Software>())
    }

    var searchTrigger by rememberSaveable { mutableIntStateOf(0) }
    var results by remember { mutableStateOf<Resource<List<RoomAvailability>>>(Resource.Loading) }

    LaunchedEffect(searchTrigger) {
        if (searchTrigger == 0) {
            results = Resource.Loading
            return@LaunchedEffect
        }
        results = Resource.Loading
        results = try {
            val target = targetDate(timeMode)
            val candidateRooms = PlanningSource.rooms.filter { info ->
                (!requireComputers || info.hasComputers) &&
                    softwareFilters.all { it in info.softwares }
            }
            if (candidateRooms.isEmpty()) {
                Resource.Success(emptyList())
            } else {
                val availabilities = coroutineScope {
                    candidateRooms.map { info ->
                        async {
                            val schedule = RoomScheduleRepository.getSchedule(info.name)
                            val matching = schedule.firstOrNull {
                                !target.before(it.debut) && target.before(it.fin)
                            }
                            RoomAvailability(name = info.name, isFree = matching == null, currentEvent = matching)
                        }
                    }.awaitAll()
                }
                Resource.Success(availabilities.sortedBy { it.name })
            }
        } catch (e: Exception) {
            Log.e("SearchScreen", "Erreur recherche", e)
            Resource.Error
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.search_title),
            color = OffWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(16.dp))

        TimeModeSelector(
            mode = timeMode,
            onSelectNow = { timeMode = TimeMode.Now },
            onSelectCustom = {
                if (timeMode !is TimeMode.Custom) {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"))
                    timeMode = TimeMode.Custom(
                        date = cal.time,
                        hour = cal.get(Calendar.HOUR_OF_DAY),
                        minute = cal.get(Calendar.MINUTE),
                    )
                }
            },
        )

        if (timeMode is TimeMode.Custom) {
            Spacer(modifier = Modifier.height(8.dp))
            CustomTimeRow(
                custom = timeMode as TimeMode.Custom,
                onDateClick = { showDatePicker = true },
                onTimeClick = { showTimePicker = true },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        FiltersSection(
            requireComputers = requireComputers,
            onToggleComputers = { requireComputers = !requireComputers },
            softwareFilters = softwareFilters,
            onToggleSoftware = { sw ->
                softwareFilters = if (sw in softwareFilters) softwareFilters - sw else softwareFilters + sw
            },
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { searchTrigger++ },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = Navy900,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.search_button), fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        when (val r = results) {
            is Resource.Loading -> if (searchTrigger > 0) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
            }
            is Resource.Error -> Text(stringResource(R.string.search_error), color = OffWhite)
            is Resource.Success -> if (r.data.isEmpty()) {
                Text(
                    stringResource(R.string.search_no_results),
                    color = SlateBlue,
                )
            } else {
                ResultsList(rooms = r.data)
            }
        }
    }

    val customMode = timeMode as? TimeMode.Custom
    if (showDatePicker && customMode != null) {
        DayPickerDialog(
            initialSelectedDate = customMode.date,
            onConfirm = { newDate ->
                timeMode = customMode.copy(date = newDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
    if (showTimePicker && customMode != null) {
        TimePickerDialog(
            initialHour = customMode.hour,
            initialMinute = customMode.minute,
            onConfirm = { h, m ->
                timeMode = customMode.copy(hour = h, minute = m)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeModeSelector(
    mode: TimeMode,
    onSelectNow: () -> Unit,
    onSelectCustom: () -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = mode is TimeMode.Now,
            onClick = onSelectNow,
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = Gold,
                activeContentColor = Navy900,
                inactiveContainerColor = Navy800,
                inactiveContentColor = OffWhite,
            ),
        ) { Text(stringResource(R.string.search_now)) }
        SegmentedButton(
            selected = mode is TimeMode.Custom,
            onClick = onSelectCustom,
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = Gold,
                activeContentColor = Navy900,
                inactiveContainerColor = Navy800,
                inactiveContentColor = OffWhite,
            ),
        ) { Text(stringResource(R.string.search_precise_time)) }
    }
}

@Composable
private fun CustomTimeRow(
    custom: TimeMode.Custom,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
) {
    val dateFmt = remember { SimpleDateFormat("EEE dd MMM", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("Europe/Paris") } }
    val dateLabel = remember(custom.date) {
        dateFmt.format(custom.date).replaceFirstChar { it.uppercase() }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Pill(text = dateLabel, onClick = onDateClick, modifier = Modifier.weight(1f))
        Pill(text = "%02d:%02d".format(custom.hour, custom.minute), onClick = onTimeClick)
    }
}

@Composable
private fun Pill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = Navy800,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text, color = OffWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersSection(
    requireComputers: Boolean,
    onToggleComputers: () -> Unit,
    softwareFilters: Set<Software>,
    onToggleSoftware: (Software) -> Unit,
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = Navy800,
        labelColor = OffWhite,
        selectedContainerColor = Gold,
        selectedLabelColor = Navy900,
        disabledContainerColor = Gold.copy(alpha = 0.5f),
        disabledSelectedContainerColor = Gold.copy(alpha = 0.5f),
        disabledLabelColor = Navy900,
    )
    val computersImplied = softwareFilters.isNotEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.search_filters), color = SlateBlue, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = requireComputers || computersImplied,
                enabled = !computersImplied,
                onClick = onToggleComputers,
                label = { Text(stringResource(R.string.search_computers)) },
                colors = chipColors,
            )
            Software.values().forEach { sw ->
                FilterChip(
                    selected = sw in softwareFilters,
                    onClick = { onToggleSoftware(sw) },
                    label = { Text(sw.displayName) },
                    colors = chipColors,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Navy800) {
            Column(modifier = Modifier.padding(20.dp)) {
                TimePicker(state = state)
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cal_cancel), color = OffWhite) }
                    TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                        Text(stringResource(R.string.cal_ok), color = Gold, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsList(rooms: List<RoomAvailability>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(rooms, key = { it.name }) { room ->
            RoomRow(room = room)
        }
    }
}

@Composable
private fun RoomRow(room: RoomAvailability) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("Europe/Paris") } }
    Surface(
        color = Navy800,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (room.isFree) FreeGreen else BusyRed,
                        shape = CircleShape,
                    ),
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    color = OffWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                val subtitle = if (room.isFree) stringResource(R.string.search_room_free)
                else room.currentEvent?.let {
                    stringResource(R.string.search_room_occupied, it.matiere, timeFmt.format(it.fin))
                } ?: stringResource(R.string.search_room_busy)
                Text(text = subtitle, color = SlateBlue, fontSize = 12.sp)
            }
        }
    }
}

private val FreeGreen = Color(0xFF4ADE80)
private val BusyRed = Color(0xFFEF4444)

private fun targetDate(mode: TimeMode): Date = when (mode) {
    TimeMode.Now -> Date()
    is TimeMode.Custom -> Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris")).apply {
        time = mode.date
        set(Calendar.HOUR_OF_DAY, mode.hour)
        set(Calendar.MINUTE, mode.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}
