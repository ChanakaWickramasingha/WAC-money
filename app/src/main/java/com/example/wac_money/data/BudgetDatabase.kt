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
            db = writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_AMOUNT, budget.amount)
                put(COLUMN_MONTH, budget.month)
                put(COLUMN_YEAR, budget.year)
                put(COLUMN_CREATED_AT, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(budget.createdAt))
            }

            val id = db.insert(TABLE_BUDGET, null, values)
            Log.d(TAG, "Budget saved with id: $id")
            return id
        } catch (e: Exception) {
            Log.e(TAG, "Error saving budget", e)
            throw e
        } finally {
            db?.close()
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
                Budget(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    month = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MONTH)),
                    year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)),
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))) ?: Date()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current budget", e)
            throw e
        } finally {
            cursor?.close()
            db?.close()
        }
    }
}
