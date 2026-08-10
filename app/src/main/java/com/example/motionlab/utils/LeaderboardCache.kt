package com.example.motionlab.utils

import android.content.Context
import com.example.motionlab.data.remote.LeaderboardEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LeaderboardCache {
    private const val PREF_NAME = "leaderboard_cache"
    private const val KEY_ENTRIES = "entries_json"
    private const val KEY_UPDATED_AT = "updated_at"

    private val gson = Gson()

    fun save(context: Context, entries: List<LeaderboardEntry>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(entries)
        prefs.edit()
            .putString(KEY_ENTRIES, json)
            .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
            .apply()
    }

    fun load(context: Context): List<LeaderboardEntry> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<LeaderboardEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getUpdatedAt(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_UPDATED_AT, 0L)
    }
}


