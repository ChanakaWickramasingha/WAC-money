package com.example.wac_money.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.wac_money.model.Transaction
import com.example.wac_money.model.TransactionCategory
import com.example.wac_money.model.TransactionType
import java.util.*

class TransactionDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "transactions.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "transactions"

        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TYPE = "type"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_DATE INTEGER NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addTransaction(transaction: Transaction): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, transaction.id)
            put(COLUMN_TITLE, transaction.title)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_CATEGORY, transaction.category.name)
            put(COLUMN_DATE, transaction.date.time)
            put(COLUMN_TYPE, transaction.type.name)
        }

        return db.insert(TABLE_NAME, null, values)
    }

    fun updateTransaction(transaction: Transaction): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, transaction.title)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_CATEGORY, transaction.category.name)
            put(COLUMN_DATE, transaction.date.time)
            put(COLUMN_TYPE, transaction.type.name)
        }

        return db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(transaction.id))
    }

    fun deleteTransaction(transactionId: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(transactionId))
    }

    fun getTransaction(transactionId: String): Transaction? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(transactionId),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val transaction = cursorToTransaction(cursor)
            cursor.close()
            transaction
        } else {
            cursor.close()
            null
        }
    }

    fun getAllTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                transactions.add(cursorToTransaction(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return transactions
    }

    private fun cursorToTransaction(cursor: android.database.Cursor): Transaction {
        val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
        val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
        val category = TransactionCategory.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)))
        val date = Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE)))
        val type = TransactionType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)))

        return Transaction(id, title, amount, category, date, type)
    }
}
