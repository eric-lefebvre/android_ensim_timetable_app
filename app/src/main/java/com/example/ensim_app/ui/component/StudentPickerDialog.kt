package com.example.ensim_app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import com.example.ensim_app.service.PlanningSource
import com.example.ensim_app.service.Student
import com.example.ensim_app.ui.theme.Gold
import com.example.ensim_app.ui.theme.Navy800
import com.example.ensim_app.ui.theme.Navy900
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue

@Composable
fun StudentPickerDialog(
    title: String,
    onPick: (Student) -> Unit,
    onDismiss: () -> Unit,
    excludeResourceId: Int? = null,
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) {
        val all = if (query.isBlank()) PlanningSource.students4AInfo
                  else PlanningSource.searchStudents(query, max = 46)
        if (excludeResourceId != null) all.filter { it.resourceId != excludeResourceId }
        else all
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Navy900,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = title,
                    color = OffWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.student_picker_hint), color = SlateBlue) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite,
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = SlateBlue,
                        cursorColor = Gold,
                        focusedContainerColor = Navy800,
                        unfocusedContainerColor = Navy800,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                ) {
                    items(results, key = { it.resourceId }) { student ->
                        StudentRow(student = student, onClick = { onPick(student) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentRow(student: Student, onClick: () -> Unit) {
    Surface(
        color = Navy800,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = formatStudentName(student.displayName),
                color = OffWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "#${student.resourceId}",
                color = SlateBlue,
                fontSize = 11.sp,
            )
        }
    }
}
