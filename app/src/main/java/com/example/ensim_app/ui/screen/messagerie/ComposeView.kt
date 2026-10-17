package com.example.ensim_app.ui.screen.messagerie

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import com.example.ensim_app.service.MailRepository
import com.example.ensim_app.service.SavedProfile
import com.example.ensim_app.service.Student
import com.example.ensim_app.ui.component.StudentPickerDialog
import com.example.ensim_app.ui.component.formatStudentName
import com.example.ensim_app.ui.theme.Gold
import com.example.ensim_app.ui.theme.Navy800
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue
import com.example.ensim_app.ui.theme.White
import kotlinx.coroutines.launch

@Composable
internal fun ComposeView(
    myProfile: SavedProfile,
    myUid: String,
    onBack: () -> Unit,
    onSent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var recipient by remember { mutableStateOf<Student?>(null) }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var pickerOpen by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }

    val canSend = recipient != null && subject.isNotBlank() && body.isNotBlank() && !sending

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.compose_back_cd), tint = OffWhite)
            }
            Text(
                text = stringResource(R.string.compose_new_mail),
                color = OffWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Surface(
            color = Navy800,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { pickerOpen = true },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.compose_to), color = SlateBlue, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = recipient?.let { formatStudentName(it.displayName) }
                            ?: stringResource(R.string.compose_choose_recipient),
                        color = if (recipient != null) OffWhite else SlateBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = OffWhite)
            }
        }

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            placeholder = { Text(stringResource(R.string.compose_subject_hint), color = SlateBlue) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = mailFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            placeholder = { Text(stringResource(R.string.compose_message_hint), color = SlateBlue) },
            colors = mailFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Button(
            onClick = {
                val to = recipient ?: return@Button
                sending = true
                scope.launch {
                    try {
                        MailRepository.sendMail(
                            fromUid = myUid,
                            fromResourceId = myProfile.resourceId,
                            fromDisplayName = myProfile.displayName,
                            toResourceId = to.resourceId,
                            toDisplayName = to.displayName,
                            subject = subject.trim(),
                            body = body.trim(),
                        )
                        Toast.makeText(context, context.getString(R.string.compose_mail_sent), Toast.LENGTH_SHORT).show()
                        onSent()
                    } catch (e: Exception) {
                        Log.e("ENSIM-MAIL", "send failed", e)
                        Toast.makeText(context, context.getString(R.string.compose_send_error, e.message), Toast.LENGTH_LONG).show()
                        sending = false
                    }
                }
            },
            enabled = canSend,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = White,
                disabledContainerColor = Navy800,
                disabledContentColor = SlateBlue,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (sending) R.string.compose_sending else R.string.compose_send), fontWeight = FontWeight.SemiBold)
        }
    }

    if (pickerOpen) {
        StudentPickerDialog(
            title = stringResource(R.string.compose_choose_recipient),
            onPick = {
                recipient = it
                pickerOpen = false
            },
            onDismiss = { pickerOpen = false },
        )
    }
}
