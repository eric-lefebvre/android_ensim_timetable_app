package com.example.ensim_app.ui.screen.messagerie

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.ensim_app.service.SavedProfile
import com.example.ensim_app.service.model.Mail
import androidx.compose.ui.res.stringResource
import com.example.ensim_app.R
import com.example.ensim_app.ui.theme.Gold
import com.example.ensim_app.ui.theme.OffWhite
import com.example.ensim_app.ui.theme.SlateBlue
import com.example.ensim_app.ui.theme.White

internal sealed class MailView {
    data object Inbox : MailView()
    data object Compose : MailView()
    data class Detail(val mail: Mail) : MailView()
}

@Composable
fun MessagerieScreen(
    myProfile: SavedProfile?,
    myUid: String?,
    firebaseError: String? = null,
    onRetryFirebase: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (myProfile == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.messagerie_login_prompt),
                color = OffWhite,
                fontSize = 14.sp,
            )
        }
        return
    }

    if (myUid == null) {
        FirebasePendingView(
            error = firebaseError,
            onRetry = onRetryFirebase,
            modifier = modifier,
        )
        return
    }

    var view by remember { mutableStateOf<MailView>(MailView.Inbox) }

    when (val current = view) {
        MailView.Inbox -> InboxView(
            myResourceId = myProfile.resourceId,
            onCompose = { view = MailView.Compose },
            onOpenMail = { view = MailView.Detail(it) },
            modifier = modifier,
        )
        MailView.Compose -> ComposeView(
            myProfile = myProfile,
            myUid = myUid,
            onBack = { view = MailView.Inbox },
            onSent = { view = MailView.Inbox },
            modifier = modifier,
        )
        is MailView.Detail -> MailDetailView(
            mail = current.mail,
            onBack = { view = MailView.Inbox },
            modifier = modifier,
        )
    }
}

@Composable
private fun FirebasePendingView(
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (error == null) {
            CircularProgressIndicator(color = Gold)
            Text(
                text = stringResource(R.string.messagerie_connecting),
                color = OffWhite,
                fontSize = 14.sp,
            )
        } else {
            Text(
                text = stringResource(R.string.messagerie_connection_failed),
                color = OffWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = error,
                color = SlateBlue,
                fontSize = 12.sp,
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = White,
                ),
            ) {
                Text(stringResource(R.string.messagerie_retry), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
