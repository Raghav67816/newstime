package com.example.newstime.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPrefManager{
    private const val PREF_NAME = "newstime_pref"
    private const val KEY_TOKEN = "token"
    private const val KEY_USERNAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_INTERESTS = "interests"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context){
        prefs = context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE)
    }

    fun setToken(token: String){
        prefs.edit().putString(token)
    }

    fun getToken(){
        return prefs.getString(KEY_TOKEN)
    }

    fun saveInterests(interests: Set<String>){
        prefs.edit().putStringSet(KEY_INTERESTS, interests)
    }

    fun getInterests(){
        return prefs.getStringSet(KEY_INTERESTS)
    }
}