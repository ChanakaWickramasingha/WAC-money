package com.example.wac_money.ui.budget

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wac_money.data.BudgetProgress
import com.example.wac_money.data.BudgetRepository
import com.example.wac_money.data.DashboardRepository
import com.example.wac_money.data.Transaction
import com.example.wac_money.util.CurrencyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "BudgetViewModel"
    }

    private val repository = BudgetRepository(application)
    private val dashboardRepository = DashboardRepository(application)
    private val currencyFormatter = CurrencyFormatter.getInstance(application)

    // LiveData for UI updates
    private val _budgetProgress = MutableLiveData<BudgetProgress>()
    val budgetProgress: LiveData<BudgetProgress> = _budgetProgress

    private val _remainingBudget = MutableLiveData<String>()
    val remainingBudget: LiveData<String> = _remainingBudget

    private val _spendingByCategory = MutableLiveData<Map<String, Double>>()
    val spendingByCategory: LiveData<Map<String, Double>> = _spendingByCategory

    private val _recentTransactions = MutableLiveData<List<Transaction>>()
    val recentTransactions: LiveData<List<Transaction>> = _recentTransactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _success = MutableLiveData<String>()
    val success: LiveData<String> = _success

    init {
        loadBudgetData()
        // Listen for currency changes
        viewModelScope.launch {
            currencyFormatter.currencyChangeEvents.collectLatest {
                Log.d(TAG, "Currency changed to: ${it.currencyCode}")
                loadBudgetData() // Reload data with new currency
            }
        }
    }

    /**
     * Load all budget data
     */
    fun loadBudgetData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                withContext(Dispatchers.IO) {
                    // Get budget progress
                    val progress = repository.getBudgetProgress()
                    _budgetProgress.postValue(progress)

                    // Get remaining budget
                    val remaining = repository.getRemainingBudget()
                    _remainingBudget.postValue(formatCurrency(remaining))

                    // Get spending by category
                    val spending = repository.getSpendingByCategory()
                    _spendingByCategory.postValue(spending)

                    // Get recent transactions
                    val recent = dashboardRepository.getRecentTransactions()
                    _recentTransactions.postValue(recent)

                    _isLoading.postValue(false)
                }

                Log.d(TAG, "Budget data loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading budget data", e)
                _error.value = "Error loading budget data: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Save a new budget
     */
    fun saveBudget(amount: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                withContext(Dispatchers.IO) {
                    repository.saveBudget(amount)
                    _success.postValue("Budget saved successfully")
                    loadBudgetData() // Reload data after saving
                }

                Log.d(TAG, "Budget saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving budget", e)
                _error.value = "Error saving budget: ${e.message}"
            } finally {
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
     * Refresh budget data
     */
    fun refresh() {
        loadBudgetData()
    }
}
