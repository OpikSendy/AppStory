package com.example.appstory.data.model

import android.content.Context

object TokenManager {

    private const val PREF_NAME = "user_prefs"
    private const val KEY_TOKEN = "auth_token"

    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_TOKEN, null)
    }
}
