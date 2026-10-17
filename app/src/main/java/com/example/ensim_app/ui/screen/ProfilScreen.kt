package com.example.ensim_app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import com.example.ensim_app.service.MailRepository
import com.example.ensim_app.util.LanguageManager
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ensim_app.service.PlanningSource
import com.example.ensim_app.service.SavedProfile
import com.example.ensim_app.service.Student
import com.example.ensim_app.ui.component.StudentPickerDialog
import com.example.ensim_app.ui.component.formatStudentName
import com.example.ensim_app.ui.component.splitStudentName
import com.example.ensim_app.ui.theme.Gold
import com.example.ensim_app.ui.theme.Navy800
import com.example.ensim_app.ui.theme.Navy900
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue
import com.example.ensim_app.ui.theme.White

@Composable
fun ProfilScreen(
    profile: SavedProfile?,
    displayedEdtId: Int?,
    myUid: String?,
    onCasLogin: () -> Unit,
    onSelectDisplayedEdt: (Student) -> Unit,
    onLogout: () -> Unit,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var seedingInbox by remember { mutableStateOf(false) }
    var pickerOpen by remember { mutableStateOf(false) }

    val currentLang = remember { LanguageManager.getLanguage(context) }

    val onSeedDemoInbox: (() -> Unit)? = if (profile != null && myUid != null) {
        {
            seedingInbox = true
            scope.launch {
                try {
                    val count = MailRepository.seedDemoInbox(
                        targetResourceId = profile.resourceId,
                        targetDisplayName = profile.displayName,
                        sentByUid = myUid,
                    )
                    Toast.makeText(
                        context,
                        context.getString(R.string.profile_demo_mails_created, count),
                        Toast.LENGTH_SHORT,
                    ).show()
                } catch (e: Exception) {
                    Log.e("ENSIM-MAIL", "seedDemoInbox failed", e)
                    Toast.makeText(
                        context,
                        context.getString(R.string.profile_seed_error, e.message),
                        Toast.LENGTH_LONG,
                    ).show()
                } finally {
                    seedingInbox = false
                }
            }
        }
    } else null

    val displayedStudent = remember(displayedEdtId) {
        displayedEdtId?.let { PlanningSource.findStudentById(it) }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Avatar(profile = profile)

        if (profile != null) {
            val (prenom, nom) = splitStudentName(profile.displayName)
            Text(
                text = "$prenom $nom".trim(),
                color = OffWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                text = stringResource(R.string.profile_not_connected),
                color = OffWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Button(
            onClick = if (profile != null) onLogout else onCasLogin,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = White,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (profile != null) stringResource(R.string.profile_logout)
                       else stringResource(R.string.profile_login_cas),
                fontWeight = FontWeight.SemiBold,
            )
        }

        DisplayedEdtRow(
            displayedEdtId = displayedEdtId,
            displayedStudent = displayedStudent,
            onClick = { pickerOpen = true },
        )

        LanguageToggleRow(
            currentLang = currentLang,
            onLanguageChange = onLanguageChange,
        )

        if (profile != null && onSeedDemoInbox != null) {
            Button(
                onClick = onSeedDemoInbox,
                enabled = !seedingInbox,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = White,
                    disabledContainerColor = Navy800,
                    disabledContentColor = SlateBlue,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (seedingInbox) stringResource(R.string.profile_sending)
                           else stringResource(R.string.profile_fill_inbox_demo),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    if (pickerOpen) {
        StudentPickerDialog(
            title = stringResource(R.string.profile_choose_schedule),
            onPick = {
                onSelectDisplayedEdt(it)
                pickerOpen = false
            },
            onDismiss = { pickerOpen = false },
        )
    }
}

@Composable
private fun LanguageToggleRow(
    currentLang: String,
    onLanguageChange: (String) -> Unit,
) {
    Surface(
        color = Navy800,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.profile_change_language_cd),
                color = SlateBlue,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LangChip(label = "FR", selected = currentLang == "fr") { onLanguageChange("fr") }
                LangChip(label = "EN", selected = currentLang == "en") { onLanguageChange("en") }
            }
        }
    }
}

@Composable
private fun LangChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Gold else Navy900)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) White else SlateBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun Avatar(profile: SavedProfile?) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(White),
        contentAlignment = Alignment.Center,
    ) {
        if (profile != null) {
            val (prenom, nom) = splitStudentName(profile.displayName)
            val initials = (nom.firstOrNull()?.toString().orEmpty() +
                prenom.firstOrNull()?.toString().orEmpty()).uppercase()
            Text(
                text = initials,
                color = Navy900,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            Text(
                text = "?",
                color = Navy900,
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DisplayedEdtRow(
    displayedEdtId: Int?,
    displayedStudent: Student?,
    onClick: () -> Unit,
) {
    val label = when {
        displayedStudent != null -> formatStudentName(displayedStudent.displayName)
        displayedEdtId != null -> stringResource(R.string.profile_unknown_schedule, displayedEdtId)
        else -> stringResource(R.string.profile_choose_schedule)
    }
    val labelColor = if (displayedEdtId == null) SlateBlue else OffWhite

    Surface(
        color = Navy800,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_displayed_schedule),
                    color = SlateBlue,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    color = labelColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = OffWhite,
            )
        }
    }
}
