package com.example.wac_money.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.Date

class TransactionDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "TransactionDatabase"
        private const val DATABASE_NAME = "transactions.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "transactions"

        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_NOTE = "note"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            val createTable = """
                CREATE TABLE $TABLE_NAME (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_TITLE TEXT NOT NULL,
                    $COLUMN_AMOUNT REAL NOT NULL,
                    $COLUMN_CATEGORY TEXT NOT NULL,
                    $COLUMN_TYPE TEXT NOT NULL,
                    $COLUMN_DATE INTEGER NOT NULL,
                    $COLUMN_NOTE TEXT
                )
            """.trimIndent()

            db.execSQL(createTable)
            Log.d(TAG, "Database table created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating database table", e)
            throw e
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
            Log.d(TAG, "Database upgraded from version $oldVersion to $newVersion")
        } catch (e: Exception) {
            Log.e(TAG, "Error upgrading database", e)
            throw e
        }
    }

    fun addTransaction(transaction: Transaction): Long {
        var db: SQLiteDatabase? = null
        try {
            db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_TITLE, transaction.title)
                put(COLUMN_AMOUNT, transaction.amount)
                put(COLUMN_CATEGORY, transaction.category)
                put(COLUMN_TYPE, transaction.type)
                put(COLUMN_DATE, transaction.date.time)
                put(COLUMN_NOTE, transaction.note)
            }
            val id = db.insert(TABLE_NAME, null, values)
            Log.d(TAG, "Transaction added with ID: $id")
            return id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding transaction", e)
            throw e
        }
    }

    fun updateTransaction(transaction: Transaction): Int {
        var db: SQLiteDatabase? = null
        try {
            db = this.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_TITLE, transaction.title)
                put(COLUMN_AMOUNT, transaction.amount)
                put(COLUMN_CATEGORY, transaction.category)
                put(COLUMN_TYPE, transaction.type)
                put(COLUMN_DATE, transaction.date.time)
                put(COLUMN_NOTE, transaction.note)
            }
            val rowsAffected = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(transaction.id.toString()))
            Log.d(TAG, "Transaction updated, rows affected: $rowsAffected")
            return rowsAffected
        } catch (e: Exception) {
            Log.e(TAG, "Error updating transaction", e)
            throw e
        }
    }

    fun deleteTransaction(id: Long): Int {
        var db: SQLiteDatabase? = null
        try {
            db = this.writableDatabase
            val rowsAffected = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
            Log.d(TAG, "Transaction deleted, rows affected: $rowsAffected")
            return rowsAffected
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting transaction", e)
            throw e
        }
    }

    fun getTransaction(id: Long): Transaction? {
        var db: SQLiteDatabase? = null
        var cursor: android.database.Cursor? = null
        try {
            db = this.readableDatabase
            cursor = db.query(
                TABLE_NAME,
                null,
                "$COLUMN_ID = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )

            return cursor.use {
                if (it.moveToFirst()) {
                    val transaction = Transaction(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                        amount = it.getDouble(it.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                        type = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE)),
                        date = Date(it.getLong(it.getColumnIndexOrThrow(COLUMN_DATE))),
                        note = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTE)) ?: ""
                    )
                    Log.d(TAG, "Transaction retrieved with ID: ${transaction.id}")
                    transaction
                } else {
                    Log.d(TAG, "No transaction found with ID: $id")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting transaction", e)
            throw e
        }
    }

    fun getAllTransactions(): List<Transaction> {
        var db: SQLiteDatabase? = null
        var cursor: android.database.Cursor? = null
        try {
            val transactions = mutableListOf<Transaction>()
            db = this.readableDatabase
            cursor = db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "$COLUMN_DATE DESC"
            )

            cursor.use {
                while (it.moveToNext()) {
                    try {
                        val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID))
                        val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                        val amount = it.getDouble(it.getColumnIndexOrThrow(COLUMN_AMOUNT))
                        val category = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))
                        val type = it.getString(it.getColumnIndexOrThrow(COLUMN_TYPE))
                        val date = Date(it.getLong(it.getColumnIndexOrThrow(COLUMN_DATE)))
                        val note = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTE)) ?: ""

                        transactions.add(
                            Transaction(
                                id = id,
                                title = title,
                                amount = amount,
                                category = category,
                                type = type,
                                date = date,
                                note = note
                            )
                        )
                        Log.d(TAG, "Added transaction with ID: $id to list")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading transaction from cursor", e)
                        // Continue with next transaction instead of failing completely
                    }
                }
            }
            Log.d(TAG, "Retrieved ${transactions.size} transactions")
            return transactions
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all transactions", e)
            throw e
        }
    }
}
