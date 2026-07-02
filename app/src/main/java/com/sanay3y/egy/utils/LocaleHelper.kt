package com.sanay3y.egy.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocaleHelper {

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun getInitialLanguage(context: Context): String {
        val prefs = PreferenceManager(context)
        return prefs.getSelectedLanguage() ?: if (Locale.getDefault().language == "ar") "ar" else "en"
    }
}
