package com.example.wac_money.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wac_money.R
import com.google.android.material.progressindicator.CircularProgressIndicator

class DashboardFragment : Fragment() {
    companion object {
        private const val TAG = "DashboardFragment"
    }

    private lateinit var viewModel: DashboardViewModel
    private lateinit var balanceAmountTextView: TextView
    private lateinit var incomeAmountTextView: TextView
    private lateinit var expenseAmountTextView: TextView
    private lateinit var recentTransactionsRecyclerView: RecyclerView
    private lateinit var recentTransactionsAdapter: RecentTransactionAdapter
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        try {
            // Initialize ViewModel
            viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
            Log.d(TAG, "ViewModel initialized")

            // Initialize views
            balanceAmountTextView = view.findViewById(R.id.balanceAmount)
            incomeAmountTextView = view.findViewById(R.id.incomeAmount)
            expenseAmountTextView = view.findViewById(R.id.expenseAmount)
            recentTransactionsRecyclerView = view.findViewById(R.id.recentTransactionsList)

            // Add a progress indicator
            progressIndicator = CircularProgressIndicator(requireContext()).apply {
                isIndeterminate = true
                visibility = View.GONE
            }
            (view as ViewGroup).addView(progressIndicator)

            // Setup RecyclerView for recent transactions
            setupRecentTransactionsRecyclerView()

            // Observe LiveData
            observeViewModel()

            Log.d(TAG, "onViewCreated completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecentTransactionsRecyclerView() {
        try {
            recentTransactionsAdapter = RecentTransactionAdapter()

            recentTransactionsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = recentTransactionsAdapter
            }
            Log.d(TAG, "Recent transactions RecyclerView setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
            Toast.makeText(requireContext(), "Error setting up list: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeViewModel() {
        try {
            // Observe total balance
            viewModel.totalBalance.observe(viewLifecycleOwner) { balance ->
                balanceAmountTextView.text = balance
                Log.d(TAG, "Total balance updated: $balance")
            }

            // Observe total income
            viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
                incomeAmountTextView.text = income
                Log.d(TAG, "Total income updated: $income")
            }

            // Observe total expenses
            viewModel.totalExpenses.observe(viewLifecycleOwner) { expenses ->
                expenseAmountTextView.text = expenses
                Log.d(TAG, "Total expenses updated: $expenses")
            }

            // Observe recent transactions
            viewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
                recentTransactionsAdapter.submitList(transactions)
                Log.d(TAG, "Recent transactions updated: ${transactions.size} items")
            }

            // Observe loading state
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
                Log.d(TAG, "Loading state updated: $isLoading")
            }

            // Observe error
            viewModel.error.observe(viewLifecycleOwner) { error ->
                if (error.isNotEmpty()) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error observed: $error")
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
