package com.example.wac_money.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "WACMoneyPrefs"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_BUDGET_ALERTS = "budget_alerts"
        private const val KEY_TRANSACTION_REMINDERS = "transaction_reminders"
    }

    var currency: String
        get() = prefs.getString(KEY_CURRENCY, "USD") ?: "USD"
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    var budgetAlertsEnabled: Boolean
        get() = prefs.getBoolean(KEY_BUDGET_ALERTS, true)
        set(value) = prefs.edit().putBoolean(KEY_BUDGET_ALERTS, value).apply()

    var transactionRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_TRANSACTION_REMINDERS, true)
        set(value) = prefs.edit().putBoolean(KEY_TRANSACTION_REMINDERS, value).apply()

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
