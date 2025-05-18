package com.example.evencat_android.classes

import android.content.Context
import java.util.*

object LocaleHelper {
    fun setLocale(context: Context, language: String): Context {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("lang", language).apply()

        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}

