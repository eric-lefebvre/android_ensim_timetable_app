package com.example.ensim_app.ui.state

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ensim_app.service.MailRepository
import com.example.ensim_app.service.SavedProfile

data class FirebaseAuthState(
    val uid: String?,
    val error: String?,
    val retry: () -> Unit,
)

@Composable
fun rememberFirebaseAuth(profile: SavedProfile?): FirebaseAuthState {
    var uid by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(profile, retryTick) {
        if (profile == null) {
            uid = null
            error = null
            return@LaunchedEffect
        }
        error = null
        try {
            val u = MailRepository.ensureSignedIn()
            uid = u
        } catch (e: Exception) {
            Log.e("ENSIM-MAIL", "Firebase init failed", e)
            uid = null
            error = e.message ?: e.javaClass.simpleName
        }
    }

    return FirebaseAuthState(uid = uid, error = error, retry = { retryTick++ })
}
