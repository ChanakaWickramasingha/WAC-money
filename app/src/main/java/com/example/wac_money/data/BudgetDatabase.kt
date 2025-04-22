package com.example.wac_money.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val TAG = "BudgetDatabase"
        private const val DATABASE_NAME = "budget.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_BUDGET = "budgets"

        private const val COLUMN_ID = "id"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_MONTH = "month"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_BUDGET (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_MONTH INTEGER NOT NULL,
                $COLUMN_YEAR INTEGER NOT NULL,
                $COLUMN_CREATED_AT TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTable)
        Log.d(TAG, "Budget table created")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET")
        onCreate(db)
    }

    fun saveBudget(budget: Budget): Long {
        var db: SQLiteDatabase? = null
        try {
            Log.d(TAG, "Opening database for writing")
            db = writableDatabase

            // Start a transaction
            db.beginTransaction()
            Log.d(TAG, "Transaction started")

            // First, delete any existing budget for the same month and year
            val deletedRows = db.delete(
                TABLE_BUDGET,
                "$COLUMN_MONTH = ? AND $COLUMN_YEAR = ?",
                arrayOf(budget.month.toString(), budget.year.toString())
            )
            Log.d(TAG, "Deleted $deletedRows existing budgets for month ${budget.month}, year ${budget.year}")

            // Now insert the new budget
            val values = ContentValues().apply {
                put(COLUMN_AMOUNT, budget.amount)
                put(COLUMN_MONTH, budget.month)
                put(COLUMN_YEAR, budget.year)
                put(COLUMN_CREATED_AT, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(budget.createdAt))
            }

            val id = db.insert(TABLE_BUDGET, null, values)
            if (id != -1L) {
                Log.d(TAG, "Budget saved with id: $id")
            } else {
                Log.e(TAG, "Failed to save budget")
                db.endTransaction()
                throw Exception("Failed to save budget")
            }

            // Mark the transaction as successful
            db.setTransactionSuccessful()
            Log.d(TAG, "Transaction marked as successful")

            // End the transaction
            db.endTransaction()
            Log.d(TAG, "Transaction ended")

            return id
        } catch (e: Exception) {
            Log.e(TAG, "Error saving budget", e)
            // Make sure to end the transaction even if there's an error
            db?.endTransaction()
            throw e
        } finally {
            db?.close()
            Log.d(TAG, "Database connection closed")
        }
    }

    fun getCurrentBudget(): Budget? {
        var db: SQLiteDatabase? = null
        var cursor: android.database.Cursor? = null
        try {
            db = readableDatabase
            val currentDate = Date()
            val calendar = java.util.Calendar.getInstance()
            val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
            val currentYear = calendar.get(java.util.Calendar.YEAR)

            Log.d(TAG, "Getting current budget for month: $currentMonth, year: $currentYear")

            cursor = db.query(
                TABLE_BUDGET,
                null,
                "$COLUMN_MONTH = ? AND $COLUMN_YEAR = ?",
                arrayOf(currentMonth.toString(), currentYear.toString()),
                null,
                null,
                "$COLUMN_CREATED_AT DESC",
                "1"
            )

            return if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
                val month = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MONTH))
                val year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR))
                val createdAtStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .parse(createdAtStr) ?: Date()

                Log.d(TAG, "Found budget: id=$id, amount=$amount, month=$month, year=$year")

                Budget(
                    id = id,
                    amount = amount,
                    month = month,
                    year = year,
                    createdAt = createdAt
                )
            } else {
                Log.d(TAG, "No budget found for current month and year")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current budget", e)
            return null
        } finally {
            cursor?.close()
            db?.close()
        }
    }
}
