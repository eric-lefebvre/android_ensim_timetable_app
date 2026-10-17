package com.example.ensim_app.service

import android.content.Context

data class SavedProfile(val resourceId: Int, val displayName: String)

object UserProfileStore {
    private const val PREFS = "user_profile"
    private const val KEY_RESOURCE_ID = "resource_id"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_DISPLAYED_EDT_ID = "displayed_edt_id"

    fun saveProfile(context: Context, resourceId: Int, displayName: String) {
        context.prefs().edit()
            .putInt(KEY_RESOURCE_ID, resourceId)
            .putString(KEY_DISPLAY_NAME, displayName)
            .apply()
    }

    fun loadProfile(context: Context): SavedProfile? {
        val prefs = context.prefs()
        if (!prefs.contains(KEY_RESOURCE_ID)) return null
        val id = prefs.getInt(KEY_RESOURCE_ID, -1)
        val name = prefs.getString(KEY_DISPLAY_NAME, null) ?: return null
        if (id < 0) return null
        return SavedProfile(resourceId = id, displayName = name)
    }

    fun saveDisplayedEdt(context: Context, resourceId: Int) {
        context.prefs().edit().putInt(KEY_DISPLAYED_EDT_ID, resourceId).apply()
    }

    fun loadDisplayedEdt(context: Context): Int? {
        val prefs = context.prefs()
        if (!prefs.contains(KEY_DISPLAYED_EDT_ID)) return null
        val id = prefs.getInt(KEY_DISPLAYED_EDT_ID, -1)
        return if (id < 0) null else id
    }

    fun clearProfile(context: Context) {
        context.prefs().edit()
            .remove(KEY_RESOURCE_ID)
            .remove(KEY_DISPLAY_NAME)
            .apply()
    }

    private fun Context.prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
