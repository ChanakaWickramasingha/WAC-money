package com.example.wac_money.ui.budget

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wac_money.data.BudgetProgress
import com.example.wac_money.data.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "BudgetViewModel"
    }

    private val repository = BudgetRepository(application)

    private val _budgetProgress = MutableLiveData<BudgetProgress>()
    val budgetProgress: LiveData<BudgetProgress> = _budgetProgress

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _success = MutableLiveData<String>()
    val success: LiveData<String> = _success

    // Currency formatter
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    init {
        loadBudgetProgress()
    }

    fun loadBudgetProgress() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val progress = withContext(Dispatchers.IO) {
                    repository.getBudgetProgress()
                }
                _budgetProgress.value = progress
                _isLoading.value = false
                Log.d(TAG, "Budget progress loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading budget progress", e)
                _error.value = "Error loading budget progress: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun saveBudget(amount: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                withContext(Dispatchers.IO) {
                    repository.saveBudget(amount)
                }
                loadBudgetProgress()
                _success.value = "Budget saved successfully"
                Log.d(TAG, "Budget saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving budget", e)
                _error.value = "Error saving budget: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun formatCurrency(amount: Double): String {
        return currencyFormatter.format(amount)
    }

    fun refresh() {
        loadBudgetProgress()
    }
}
