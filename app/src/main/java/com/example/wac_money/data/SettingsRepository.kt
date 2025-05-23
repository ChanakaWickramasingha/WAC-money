package com.example.wac_money.data

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_CURRENCY_CODE = "currency_code"
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val DEFAULT_CURRENCY_CODE = "USD"
        private const val DEFAULT_CURRENCY_SYMBOL = "$"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSettings(): Settings {
        val currencyCode = prefs.getString(KEY_CURRENCY_CODE, DEFAULT_CURRENCY_CODE) ?: DEFAULT_CURRENCY_CODE
        val currencySymbol = prefs.getString(KEY_CURRENCY_SYMBOL, DEFAULT_CURRENCY_SYMBOL) ?: DEFAULT_CURRENCY_SYMBOL
        return Settings(currencyCode, currencySymbol)
    }

    fun saveSettings(settings: Settings) {
        prefs.edit().apply {
            putString(KEY_CURRENCY_CODE, settings.currencyCode)
            putString(KEY_CURRENCY_SYMBOL, settings.currencySymbol)
            apply()
        }
    }
}
