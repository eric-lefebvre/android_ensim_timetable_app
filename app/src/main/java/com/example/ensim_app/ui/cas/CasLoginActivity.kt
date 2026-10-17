package com.example.ensim_app.ui.cas

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ensim_app.service.CasIdentity
import com.example.ensim_app.service.CasService
import com.example.ensim_app.ui.theme.ENSIM_appTheme
import kotlinx.coroutines.launch

class CasLoginActivity : ComponentActivity() {

    companion object {
        const val RESULT_USERNAME = "cas_username"
        const val RESULT_ATTRIBUTES = "cas_attributes"
        const val RESULT_ERROR = "cas_error"

        fun newIntent(context: Context): Intent = Intent(context, CasLoginActivity::class.java)

        fun extractIdentity(data: Intent): CasIdentity? {
            val username = data.getStringExtra(RESULT_USERNAME) ?: return null
            val bundle = data.getBundleExtra(RESULT_ATTRIBUTES) ?: return CasIdentity(username, emptyMap())
            val attributes = bundle.keySet().associateWith { bundle.getString(it).orEmpty() }
            return CasIdentity(username = username, attributes = attributes)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Pas de session précédente : oblige un nouveau login
        CookieManager.getInstance().removeAllCookies(null)
        setContent {
            ENSIM_appTheme {
                CasLoginContent(
                    onSuccess = { identity ->
                        setResult(Activity.RESULT_OK, identity.toIntent())
                        finish()
                    },
                    onFailure = { reason ->
                        setResult(
                            Activity.RESULT_CANCELED,
                            Intent().putExtra(RESULT_ERROR, reason),
                        )
                        finish()
                    },
                )
            }
        }
    }

    private fun CasIdentity.toIntent(): Intent {
        val attrBundle = Bundle().apply {
            attributes.forEach { (k, v) -> putString(k, v) }
        }
        return Intent().apply {
            putExtra(RESULT_USERNAME, username)
            putExtra(RESULT_ATTRIBUTES, attrBundle)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@androidx.compose.runtime.Composable
private fun CasLoginContent(
    onSuccess: (CasIdentity) -> Unit,
    onFailure: (String) -> Unit,
) {
    var validating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false
                            val ticket = extractTicketFromCallback(url) ?: return false
                            Log.d("ENSIM-CAS", "Ticket capturé: $ticket")
                            validating = true
                            scope.launch {
                                try {
                                    val identity = CasService.validateTicket(ticket)
                                    Log.d("ENSIM-CAS", "Identité: $identity")
                                    onSuccess(identity)
                                } catch (e: Exception) {
                                    Log.e("ENSIM-CAS", "Validation échouée", e)
                                    onFailure(e.message ?: "Erreur validation CAS")
                                }
                            }
                            return true
                        }
                    }
                    loadUrl(CasService.loginUrl())
                }
            },
        )
        if (validating) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

// Détecte une URL de la forme planning.univ-lemans.fr/direct/myplanning.jsp?ticket=ST-...
private fun extractTicketFromCallback(url: String): String? {
    if (!url.startsWith(CasService.SERVICE_URL)) return null
    val ticketParam = url.substringAfter("ticket=", "").substringBefore('&')
    return ticketParam.takeIf { it.startsWith("ST-") }
}
