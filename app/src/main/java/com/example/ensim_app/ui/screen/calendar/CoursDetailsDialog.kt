package com.example.ensim_app.ui.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import com.example.ensim_app.service.model.Cours
import com.example.ensim_app.ui.theme.Navy800
import com.example.ensim_app.ui.theme.Navy900
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun CoursDetailsDialog(cours: Cours, color: Color, onDismiss: () -> Unit) {
    val tz = TimeZone.getTimeZone("Europe/Paris")
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).apply { timeZone = tz } }
    val dateFmt = remember { SimpleDateFormat("EEEE dd MMMM", Locale.getDefault()).apply { timeZone = tz } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = OffWhite,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(color),
                )
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = cours.matiere,
                        color = Navy900,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    DetailRow(
                        label = stringResource(R.string.cours_date),
                        value = dateFmt.format(cours.debut).replaceFirstChar { it.uppercase() },
                    )
                    DetailRow(
                        label = stringResource(R.string.cours_time),
                        value = "${timeFmt.format(cours.debut)} – ${timeFmt.format(cours.fin)}",
                    )
                    DetailRow(
                        label = stringResource(if (cours.professeurs.size > 1) R.string.cours_teachers else R.string.cours_teacher),
                        value = if (cours.professeurs.isEmpty()) stringResource(R.string.cours_empty)
                            else cours.professeurs.joinToString("\n") { it.toString() },
                    )
                    DetailRow(
                        label = stringResource(R.string.cours_class),
                        value = cours.classe.ifBlank { stringResource(R.string.cours_empty) },
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Navy800,
                                contentColor = OffWhite,
                            ),
                        ) {
                            Text(stringResource(R.string.cours_close), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, color = SlateBlue, fontSize = 12.sp)
        Text(text = value, color = Navy900, fontSize = 14.sp)
    }
}
