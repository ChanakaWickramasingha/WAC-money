package com.example.wac_money.ui.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wac_money.data.Settings
import com.example.wac_money.data.SettingsRepository
import com.example.wac_money.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val settingsRepository = SettingsRepository(application)
    private val currencyFormatter = CurrencyFormatter.getInstance(application)

    private val _settings = MutableStateFlow(settingsRepository.getSettings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _success = MutableLiveData<String>()
    val success: LiveData<String> = _success

    init {
        loadSettings()
    }

    private fun loadSettings() {
        try {
            _settings.value = settingsRepository.getSettings()
            Log.d(TAG, "Settings loaded: ${_settings.value}")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading settings", e)
            _error.value = "Error loading settings: ${e.message}"
        }
    }

    fun updateCurrency(currencyCode: String, currencySymbol: String) {
        viewModelScope.launch {
            val newSettings = Settings(currencyCode, currencySymbol)
            settingsRepository.saveSettings(newSettings)
            _settings.value = newSettings
            // Notify the CurrencyFormatter of the change
            currencyFormatter.notifyCurrencyChange()
            _success.value = "Currency updated successfully"
            Log.d(TAG, "Currency updated: $newSettings")
        }
    }

    fun getFormattedAmount(amount: Double): String {
        return currencyFormatter.format(amount)
    }
}
