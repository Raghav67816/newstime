package com.example.newstime.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPrefManager {
    private const val PREF_NAME = "newstime_pref"
    private const val KEY_TOKEN = "token"
    private const val KEY_UID = "uid"
    private const val KEY_USERNAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_INTERESTS = "interests"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(name: String, email: String, token: String){
        prefs.edit{
            putString(KEY_TOKEN, token)
            putString(KEY_USERNAME, name)
            putString(KEY_EMAIL, email)
        }
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveInterests(interests: String) {
        prefs.edit { putString(KEY_INTERESTS, interests) }
    }

    fun getInterests(): Set<String>? {
        return prefs.getStringSet(KEY_INTERESTS, emptySet())
    }

    fun saveUid(uid: String){
        prefs.edit{ putString(KEY_UID, null) }
    }

    fun getUid(): String?{
        return prefs.getString(KEY_UID, null)
    }
}
