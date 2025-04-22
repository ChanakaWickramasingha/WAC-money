package com.example.wac_money.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.wac_money.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    companion object {
        private const val TAG = "SettingsFragment"
    }

    private lateinit var viewModel: SettingsViewModel
    private lateinit var currencyDropdown: MaterialAutoCompleteTextView
    private lateinit var addCurrencyButton: MaterialButton
    private var selectedCurrency: Currency? = null

    // Available currencies
    private val currencies = listOf(
        Currency("USD", "$", "US Dollar"),
        Currency("EUR", "€", "Euro"),
        Currency("GBP", "£", "British Pound"),
        Currency("JPY", "¥", "Japanese Yen"),
        Currency("INR", "₹", "Indian Rupee"),
        Currency("LKR", "Rs", "Sri Lankan Rupee"),
        Currency("AUD", "A$", "Australian Dollar"),
        Currency("CAD", "C$", "Canadian Dollar"),
        Currency("CHF", "Fr", "Swiss Franc"),
        Currency("CNY", "¥", "Chinese Yuan")
    )

    data class Currency(
        val code: String,
        val symbol: String,
        val name: String
    ) {
        override fun toString(): String = "$code ($symbol) - $name"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        try {
            // Initialize ViewModel
            viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
            Log.d(TAG, "ViewModel initialized")

            // Initialize views
            currencyDropdown = view.findViewById(R.id.currencyDropdown)
            addCurrencyButton = view.findViewById(R.id.addCurrencyButton)

            // Setup currency dropdown
            setupCurrencyDropdown()

            // Setup add button
            setupAddButton()

            // Observe ViewModel
            observeViewModel()

            Log.d(TAG, "onViewCreated completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupCurrencyDropdown() {
        try {
            // Create adapter for currency dropdown
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                currencies
            )
            currencyDropdown.setAdapter(adapter)

            // Set click listener for currency selection
            currencyDropdown.setOnItemClickListener { _, _, position, _ ->
                selectedCurrency = currencies[position]
                Log.d(TAG, "Currency selected: $selectedCurrency")
                // Don't update the currency immediately, wait for the Add button click
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up currency dropdown", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupAddButton() {
        addCurrencyButton.setOnClickListener {
            try {
                if (selectedCurrency != null) {
                    Log.d(TAG, "Add button clicked, updating currency: $selectedCurrency")
                    viewModel.updateCurrency(selectedCurrency!!.code, selectedCurrency!!.symbol)
                } else {
                    Log.w(TAG, "Add button clicked but no currency selected")
                    Toast.makeText(requireContext(), getString(R.string.select_currency_first), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling add button click", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeViewModel() {
        try {
            // Observe settings
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.settings.collectLatest { settings ->
                    Log.d(TAG, "Settings updated: $settings")
                    val currentCurrency = "${settings.currencyCode} (${settings.currencySymbol})"
                    currencyDropdown.setText(currentCurrency, false)
                }
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
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing ViewModel", e)
            Toast.makeText(requireContext(), "Error observing data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
