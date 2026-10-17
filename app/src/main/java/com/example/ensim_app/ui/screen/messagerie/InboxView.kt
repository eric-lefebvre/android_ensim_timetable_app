package com.example.ensim_app.ui.screen.messagerie

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import com.example.ensim_app.service.MailRepository
import com.example.ensim_app.service.model.Mail
import com.example.ensim_app.ui.component.formatStudentName
import com.example.ensim_app.ui.theme.Gold
import com.example.ensim_app.ui.theme.Navy800
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue
import com.example.ensim_app.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun InboxView(
    myResourceId: Int,
    onCompose: () -> Unit,
    onOpenMail: (Mail) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val inboxFlow = remember(myResourceId) { MailRepository.inboxStream(myResourceId) }
    val sentFlow = remember(myResourceId) { MailRepository.sentStream(myResourceId) }
    val inboxMails by inboxFlow.collectAsState(initial = emptyList())
    val sentMails by sentFlow.collectAsState(initial = emptyList())

    val currentMails = if (selectedTab == 0) inboxMails else sentMails

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.nav_messages),
            color = OffWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Navy800,
            contentColor = Gold,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(R.string.inbox_received), fontWeight = FontWeight.SemiBold) },
                selectedContentColor = Gold,
                unselectedContentColor = SlateBlue,
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(stringResource(R.string.inbox_sent), fontWeight = FontWeight.SemiBold) },
                selectedContentColor = Gold,
                unselectedContentColor = SlateBlue,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (currentMails.isEmpty()) {
                Text(
                    text = stringResource(R.string.inbox_empty),
                    color = SlateBlue,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentMails, key = { it.id }) { mail ->
                        MailRow(
                            mail = mail,
                            showSender = selectedTab == 0,
                            onClick = { onOpenMail(mail) },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onCompose,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = White,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(R.string.inbox_compose), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MailRow(mail: Mail, showSender: Boolean, onClick: () -> Unit) {
    val timeFmt = remember {
        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Europe/Paris")
        }
    }
    val dateText = mail.sentAt?.toDate()?.let { timeFmt.format(it) } ?: "—"
    val isUnread = !mail.read && showSender
    val weight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal
    val contactName = if (showSender) mail.fromDisplayName else mail.toDisplayName

    Surface(
        color = Navy800,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp, end = 10.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isUnread) Gold else Navy800),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatStudentName(contactName),
                        color = OffWhite,
                        fontSize = 14.sp,
                        fontWeight = weight,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = dateText,
                        color = SlateBlue,
                        fontSize = 11.sp,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = mail.subject.ifBlank { stringResource(R.string.inbox_no_subject) },
                    color = OffWhite,
                    fontSize = 14.sp,
                    fontWeight = weight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (mail.body.isNotBlank()) {
                    Text(
                        text = mail.body,
                        color = SlateBlue,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
