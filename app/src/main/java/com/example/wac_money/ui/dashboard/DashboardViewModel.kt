package com.example.wac_money.ui.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wac_money.data.DashboardRepository
import com.example.wac_money.data.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val repository = DashboardRepository(application)

    // LiveData for UI updates
    private val _totalBalance = MutableLiveData<String>()
    val totalBalance: LiveData<String> = _totalBalance

    private val _totalIncome = MutableLiveData<String>()
    val totalIncome: LiveData<String> = _totalIncome

    private val _totalExpenses = MutableLiveData<String>()
    val totalExpenses: LiveData<String> = _totalExpenses

    private val _recentTransactions = MutableLiveData<List<Transaction>>()
    val recentTransactions: LiveData<List<Transaction>> = _recentTransactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Currency formatter
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    init {
        loadDashboardData()
    }

    /**
     * Load all dashboard data
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                withContext(Dispatchers.IO) {
                    // Calculate totals
                    val balance = repository.calculateTotalBalance()
                    val income = repository.calculateTotalIncome()
                    val expenses = repository.calculateTotalExpenses()

                    // Get recent transactions
                    val recent = repository.getRecentTransactions()

                    // Update LiveData on main thread
                    withContext(Dispatchers.Main) {
                        _totalBalance.value = formatCurrency(balance)
                        _totalIncome.value = formatCurrency(income)
                        _totalExpenses.value = formatCurrency(expenses)
                        _recentTransactions.value = recent
                        _isLoading.value = false
                    }
                }

                Log.d(TAG, "Dashboard data loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading dashboard data", e)
                _error.value = "Error loading dashboard data: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Format a double value as currency
     */
    private fun formatCurrency(amount: Double): String {
        return currencyFormatter.format(amount)
    }

    /**
     * Refresh dashboard data
     */
    fun refresh() {
        loadDashboardData()
    }
}
