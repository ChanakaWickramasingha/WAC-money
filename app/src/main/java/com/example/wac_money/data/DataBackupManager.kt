package com.example.wac_money.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataBackupManager(private val context: Context) {
    companion object {
        private const val TAG = "DataBackupManager"
        private const val BACKUP_DIR = "backups"
        private const val BACKUP_FILE_PREFIX = "wac_money_backup_"
        private const val BACKUP_FILE_EXTENSION = ".json"
    }

    private val gson = Gson()
    private val transactionDatabase = TransactionDatabase(context)
    private val budgetDatabase = BudgetDatabase(context)

    data class BackupData(
        val transactions: List<Transaction>,
        val budgets: List<Budget>,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun exportData(): String {
        try {
            // Create backup directory if it doesn't exist
            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Get all data
            val transactions = transactionDatabase.getAllTransactions()
            val budgets = budgetDatabase.getAllBudgets()

            // Create backup data object
            val backupData = BackupData(
                transactions = transactions,
                budgets = budgets
            )

            // Convert to JSON
            val jsonData = gson.toJson(backupData)

            // Create backup file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "${BACKUP_FILE_PREFIX}${timestamp}${BACKUP_FILE_EXTENSION}")

            // Write to file
            backupFile.writeText(jsonData)

            Log.d(TAG, "Data exported successfully to: ${backupFile.absolutePath}")
            return backupFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting data", e)
            throw e
        }
    }

    fun importData(backupFilePath: String) {
        try {
            // Read backup file
            val backupFile = File(backupFilePath)
            if (!backupFile.exists()) {
                throw Exception("Backup file not found")
            }

            // Read and parse JSON
            val jsonData = backupFile.readText()
            val type = object : TypeToken<BackupData>() {}.type
            val backupData = gson.fromJson<BackupData>(jsonData, type)

            // Clear existing data
            transactionDatabase.clearAllTransactions()
            budgetDatabase.clearAllBudgets()

            // Restore transactions
            backupData.transactions.forEach { transaction ->
                transactionDatabase.addTransaction(transaction)
            }

            // Restore budgets
            backupData.budgets.forEach { budget ->
                budgetDatabase.saveBudget(budget)
            }

            Log.d(TAG, "Data imported successfully from: $backupFilePath")
        } catch (e: Exception) {
            Log.e(TAG, "Error importing data", e)
            throw e
        }
    }

    fun getBackupFiles(): List<File> {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        if (!backupDir.exists()) {
            return emptyList()
        }
        return backupDir.listFiles { file ->
            file.name.startsWith(BACKUP_FILE_PREFIX) && file.name.endsWith(BACKUP_FILE_EXTENSION)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun clearAllData() {
        try {
            transactionDatabase.clearAllTransactions()
            budgetDatabase.clearAllBudgets()
            Log.d(TAG, "All data cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data", e)
            throw e
        }
    }
}
