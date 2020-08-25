package com.consultantapp.utils

import android.content.Context
import android.preference.PreferenceManager
import java.util.*


/**
 * Created by cbl29 on 22/2/17.
 */
object LocaleHelper {

    fun onCreate(context: Context): Context {
        val lang = PreferenceManager.getDefaultSharedPreferences(context).getString(USER_LANGUAGE, "")?:""
        return updateResources(context, lang)
    }

    fun onCreate(context: Context, defaultLanguage: String) {
        val lang = getPersistedData(context, defaultLanguage)
        //setLocale(context, lang)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(
            context,
            Locale.getDefault().language
        )
    }

    fun setLocale(context: Context, language: String, prefsManager: PrefsManager) {
        persist(context, language, prefsManager)
        updateResources(context, language)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String {
        //return PrefsManager.get().getString(USER_LANGUAGE, "en")
        return ""
    }

    private fun persist(context: Context, language: String, prefsManager: PrefsManager) {
        prefsManager.save(language, USER_LANGUAGE)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context.createConfigurationContext(configuration)
    }

}