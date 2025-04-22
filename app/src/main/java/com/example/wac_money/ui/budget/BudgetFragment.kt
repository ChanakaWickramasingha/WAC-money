package com.example.wac_money.ui.budget

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.wac_money.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class BudgetFragment : Fragment() {
    companion object {
        private const val TAG = "BudgetFragment"
    }

    private lateinit var viewModel: BudgetViewModel
    private lateinit var budgetGauge: BudgetGaugeView
    private lateinit var budgetInputLayout: TextInputLayout
    private lateinit var budgetInput: TextInputEditText
    private lateinit var saveBudgetButton: MaterialButton
    private lateinit var warningText: View
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        try {
            // Initialize ViewModel
            viewModel = ViewModelProvider(this)[BudgetViewModel::class.java]
            Log.d(TAG, "ViewModel initialized")

            // Initialize views
            budgetGauge = view.findViewById(R.id.budgetGauge)
            budgetInputLayout = view.findViewById(R.id.budgetInputLayout)
            budgetInput = view.findViewById(R.id.budgetInput)
            saveBudgetButton = view.findViewById(R.id.saveBudgetButton)
            warningText = view.findViewById(R.id.warningText)

            // Add progress indicator
            progressIndicator = CircularProgressIndicator(requireContext()).apply {
                isIndeterminate = true
                visibility = View.GONE
            }
            (view as ViewGroup).addView(progressIndicator)

            // Setup click listeners
            setupClickListeners()

            // Observe LiveData
            observeViewModel()

            Log.d(TAG, "onViewCreated completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        saveBudgetButton.setOnClickListener {
            try {
                val amount = budgetInput.text.toString().toDoubleOrNull()
                if (amount != null && amount > 0) {
                    viewModel.saveBudget(amount)
                    budgetInput.text?.clear()
                } else {
                    budgetInputLayout.error = "Please enter a valid amount"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving budget", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeViewModel() {
        try {
            // Observe budget progress
            viewModel.budgetProgress.observe(viewLifecycleOwner) { progress ->
                budgetGauge.updateProgress(progress)
                warningText.visibility = if (progress.isWarning || progress.isExceeded) View.VISIBLE else View.GONE
                Log.d(TAG, "Budget progress updated: ${progress.progress}")
            }

            // Observe loading state
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
                saveBudgetButton.isEnabled = !isLoading
                Log.d(TAG, "Loading state updated: $isLoading")
            }

            // Observe error
            viewModel.error.observe(viewLifecycleOwner) { error ->
                if (error.isNotEmpty()) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error observed: $error")
                }
            }

            // Observe success
            viewModel.success.observe(viewLifecycleOwner) { success ->
                if (success.isNotEmpty()) {
                    Toast.makeText(requireContext(), success, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Success observed: $success")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing ViewModel", e)
            Toast.makeText(requireContext(), "Error observing data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        viewModel.refresh()
    }
}
