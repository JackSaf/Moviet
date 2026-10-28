package com.jacks.moviet.sdk

import android.content.SharedPreferences

internal class PreferencesVisitStore(private val preferences: SharedPreferences) : SecondVisitPolicy.VisitStore {
    override var foregroundCount: Int
        get() = preferences.getInt(FOREGROUND_COUNT, 0)
        set(value) { preferences.edit().putInt(FOREGROUND_COUNT, value).apply() }
}

private const val FOREGROUND_COUNT = "foreground_count"
