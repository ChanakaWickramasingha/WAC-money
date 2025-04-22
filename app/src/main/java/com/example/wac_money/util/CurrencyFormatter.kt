package com.example.wac_money.util

import android.content.Context
import com.example.wac_money.data.CurrencyChangeEvent
import com.example.wac_money.data.Settings
import com.example.wac_money.data.SettingsRepository
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CurrencyFormatter private constructor(context: Context) {
    companion object {
        private var instance: CurrencyFormatter? = null
        private const val DEFAULT_CURRENCY_CODE = "USD"
        private const val DEFAULT_CURRENCY_SYMBOL = "$"

        @Synchronized
        fun getInstance(context: Context): CurrencyFormatter {
            return instance ?: CurrencyFormatter(context.applicationContext).also { instance = it }
        }
    }

    private val settingsRepository = SettingsRepository(context)
    private var currentSettings = settingsRepository.getSettings()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Event bus for currency changes
    private val _currencyChangeEvents = MutableSharedFlow<CurrencyChangeEvent>()
    val currencyChangeEvents: SharedFlow<CurrencyChangeEvent> = _currencyChangeEvents.asSharedFlow()

    fun format(amount: Double): String {
        val settings = settingsRepository.getSettings()
        if (settings != currentSettings) {
            currentSettings = settings
            // Notify listeners of currency change
            notifyCurrencyChange(settings)
        }

        val symbols = DecimalFormatSymbols().apply {
            currencySymbol = currentSettings.currencySymbol
            decimalSeparator = '.'
            groupingSeparator = ','
        }

        val formatter = DecimalFormat("#,##0.00", symbols)
        return formatter.format(amount)
    }

    fun getCurrencySymbol(): String {
        return currentSettings.currencySymbol
    }

    fun getCurrencyCode(): String {
        return currentSettings.currencyCode
    }

    private fun notifyCurrencyChange(settings: Settings) {
        scope.launch {
            _currencyChangeEvents.emit(CurrencyChangeEvent(settings.currencyCode, settings.currencySymbol))
        }
    }

    // Method to manually trigger a currency change notification
    fun notifyCurrencyChange() {
        val settings = settingsRepository.getSettings()
        notifyCurrencyChange(settings)
    }
}
