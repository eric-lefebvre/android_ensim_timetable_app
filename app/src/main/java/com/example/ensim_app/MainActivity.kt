package com.example.ensim_app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ensim_app.service.PlanningSource
import com.example.ensim_app.service.SavedProfile
import com.example.ensim_app.service.UserProfileStore
import com.example.ensim_app.ui.cas.CasLoginActivity
import com.example.ensim_app.ui.screen.AppTab
import com.example.ensim_app.ui.screen.BottomNavBar
import com.example.ensim_app.ui.screen.ProfilScreen
import com.example.ensim_app.ui.screen.calendar.CalendarScreen
import com.example.ensim_app.ui.screen.messagerie.MessagerieScreen
import com.example.ensim_app.ui.screen.search.SearchScreen
import com.example.ensim_app.ui.state.rememberCoursForEdt
import com.example.ensim_app.ui.state.rememberFirebaseAuth
import com.example.ensim_app.ui.theme.ENSIM_appTheme
import com.example.ensim_app.R
import com.example.ensim_app.util.LanguageManager


class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ENSIM_appTheme {
                val context = LocalContext.current
                val appContext = context.applicationContext
                var profile by remember {
                    mutableStateOf(UserProfileStore.loadProfile(appContext))
                }
                var displayedEdtId by remember {
                    mutableStateOf(UserProfileStore.loadDisplayedEdt(appContext))
                }

                val effectiveEdtId = displayedEdtId ?: profile?.resourceId
                val firebaseAuth = rememberFirebaseAuth(profile)
                val coursResource = rememberCoursForEdt(effectiveEdtId)

                val casLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    handleCasResult(result, context, appContext) { profile = it }
                }

                val navController = rememberNavController()
                val currentDestination by navController.currentBackStackEntryAsState()
                val selectedTab = AppTab.entries.find {
                    it.name == currentDestination?.destination?.route
                } ?: AppTab.CALENDRIER

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavBar(
                            selectedTab = selectedTab,
                            onSelect = { tab ->
                                navController.navigate(tab.name) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    },
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)
                    NavHost(
                        navController = navController,
                        startDestination = AppTab.CALENDRIER.name,
                    ) {
                        composable(AppTab.CALENDRIER.name) {
                            CalendarScreen(coursResource = coursResource, modifier = screenModifier)
                        }
                        composable(AppTab.RECHERCHE.name) {
                            SearchScreen(modifier = screenModifier)
                        }
                        composable(AppTab.MESSAGERIE.name) {
                            MessagerieScreen(
                                myProfile = profile,
                                myUid = firebaseAuth.uid,
                                firebaseError = firebaseAuth.error,
                                onRetryFirebase = firebaseAuth.retry,
                                modifier = screenModifier,
                            )
                        }
                        composable(AppTab.PROFIL.name) {
                            ProfilScreen(
                                profile = profile,
                                displayedEdtId = effectiveEdtId,
                                myUid = firebaseAuth.uid,
                                onCasLogin = {
                                    casLauncher.launch(CasLoginActivity.newIntent(context))
                                },
                                onSelectDisplayedEdt = { student ->
                                    UserProfileStore.saveDisplayedEdt(appContext, student.resourceId)
                                    displayedEdtId = student.resourceId
                                },
                                onLogout = {
                                    UserProfileStore.clearProfile(appContext)
                                    profile = null
                                },
                                onLanguageChange = { lang ->
                                    LanguageManager.setLanguage(appContext, lang)
                                    recreate()
                                },
                                modifier = screenModifier,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleCasResult(
        result: ActivityResult,
        context: Context,
        appContext: Context,
        onSuccess: (SavedProfile) -> Unit,
    ) {
        if (result.resultCode != Activity.RESULT_OK || result.data == null) {
            val reason = result.data?.getStringExtra(CasLoginActivity.RESULT_ERROR)
            if (reason != null) {
                Toast.makeText(context, context.getString(R.string.cas_cancelled, reason), Toast.LENGTH_LONG).show()
            }
            return
        }
        val identity = CasLoginActivity.extractIdentity(result.data!!)
        if (identity == null) {
            Toast.makeText(context, context.getString(R.string.cas_invalid_identity), Toast.LENGTH_LONG).show()
            return
        }
        Log.d("ENSIM-CAS", "CAS result: $identity")
        val student = PlanningSource.matchCasIdentity(identity)
        if (student == null) {
            Toast.makeText(
                context,
                context.getString(R.string.cas_student_not_found, identity.bestLabel),
                Toast.LENGTH_LONG,
            ).show()
            return
        }
        UserProfileStore.saveProfile(appContext, student.resourceId, student.displayName)
        onSuccess(SavedProfile(student.resourceId, student.displayName))
    }
}
