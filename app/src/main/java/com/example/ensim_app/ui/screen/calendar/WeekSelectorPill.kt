package com.example.ensim_app.ui.screen.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import com.example.ensim_app.ui.component.WeekPickerDialog
import com.example.ensim_app.ui.theme.Navy800
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.util.mondayOfLocal
import com.example.ensim_app.util.weeksBetween
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun WeekSelectorPill(
    weekDates: List<Date>,
    today: Date,
    onPickOffset: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fmt = remember { SimpleDateFormat("dd/MM", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("Europe/Paris") } }
    var showPicker by remember { mutableStateOf(false) }

    Surface(
        color = Navy800,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.clickable { showPicker = true },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.week_label, fmt.format(weekDates.first()), fmt.format(weekDates.last())),
                color = OffWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = OffWhite,
            )
        }
    }

    if (showPicker) {
        WeekPickerDialog(
            initialSelectedDate = weekDates.first(),
            onConfirm = { picked ->
                onPickOffset(weeksBetween(mondayOfLocal(today), mondayOfLocal(picked)))
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}
