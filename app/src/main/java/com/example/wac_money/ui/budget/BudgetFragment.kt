package com.example.wac_money.ui.budget

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
    private lateinit var contentLayout: LinearLayout

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
            contentLayout = view.findViewById(R.id.contentLayout)

            // Add progress indicator
            progressIndicator = CircularProgressIndicator(requireContext()).apply {
                isIndeterminate = true
                visibility = View.GONE
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
            }
            contentLayout.addView(progressIndicator)

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
                Log.d(TAG, "Save budget button clicked")
                val inputText = budgetInput.text.toString()
                Log.d(TAG, "Input text: $inputText")

                if (inputText.isEmpty()) {
                    budgetInputLayout.error = "Please enter an amount"
                    return@setOnClickListener
                }

                val amount = inputText.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    Log.d(TAG, "Saving budget: $amount")
                    budgetInput.clearFocus() // Clear focus before saving
                    viewModel.saveBudget(amount)
                    budgetInputLayout.error = null
                } else {
                    Log.e(TAG, "Invalid amount: $inputText")
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
                Log.d(TAG, "Budget progress updated: budget=${progress.budget?.amount}, spending=${progress.spending}, progress=${progress.progress}")
                budgetGauge.updateProgress(progress)
                warningText.visibility = if (progress.isWarning || progress.isExceeded) View.VISIBLE else View.GONE
                Log.d(TAG, "Budget gauge updated with progress: ${progress.progress}")
            }

            // Observe loading state
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                Log.d(TAG, "Loading state updated: $isLoading")
                progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
                saveBudgetButton.isEnabled = !isLoading
                budgetInput.isEnabled = !isLoading
            }

            // Observe error
            viewModel.error.observe(viewLifecycleOwner) { error ->
                if (error.isNotEmpty()) {
                    Log.e(TAG, "Error observed: $error")
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
            }

            // Observe success
            viewModel.success.observe(viewLifecycleOwner) { success ->
                if (success.isNotEmpty()) {
                    Log.d(TAG, "Success observed: $success")
                    Toast.makeText(requireContext(), success, Toast.LENGTH_SHORT).show()
                    budgetInput.text?.clear() // Clear input on success
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
