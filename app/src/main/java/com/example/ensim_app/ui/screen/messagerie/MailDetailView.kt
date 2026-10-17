package com.example.ensim_app.ui.screen.messagerie

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import com.example.ensim_app.service.MailRepository
import com.example.ensim_app.service.model.Mail
import com.example.ensim_app.ui.component.formatStudentName
import com.example.ensim_app.ui.theme.Navy800
import com.example.ensim_app.ui.theme.Navy900
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun MailDetailView(
    mail: Mail,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    LaunchedEffect(mail.id) {
        if (!mail.read && mail.id.isNotEmpty()) {
            try {
                MailRepository.markRead(mail.id)
            } catch (e: Exception) {
                Log.e("ENSIM-MAIL", "markRead failed", e)
            }
        }
    }

    val dateFmt = remember { SimpleDateFormat("EEEE dd MMMM yyyy, HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("Europe/Paris") } }
    val dateText = mail.sentAt?.toDate()?.let { dateFmt.format(it) }
        ?.replaceFirstChar { it.uppercase() } ?: "—"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.compose_back_cd), tint = OffWhite)
            }
            Text(
                text = stringResource(R.string.mail_title),
                color = OffWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = mail.subject.ifBlank { stringResource(R.string.mail_no_subject) },
            color = OffWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Surface(color = Navy800, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                LabelValue(stringResource(R.string.mail_from), formatStudentName(mail.fromDisplayName) + " (#${mail.fromResourceId})")
                Spacer(modifier = Modifier.height(6.dp))
                LabelValue(stringResource(R.string.mail_to), formatStudentName(mail.toDisplayName) + " (#${mail.toResourceId})")
                Spacer(modifier = Modifier.height(6.dp))
                LabelValue(stringResource(R.string.mail_date), dateText)
            }
        }

        Surface(color = Navy900, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = mail.body.ifBlank { stringResource(R.string.mail_empty_body) },
                    color = OffWhite,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Row {
        Text(
            text = "$label : ",
            color = SlateBlue,
            fontSize = 13.sp,
        )
        Text(
            text = value,
            color = OffWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
