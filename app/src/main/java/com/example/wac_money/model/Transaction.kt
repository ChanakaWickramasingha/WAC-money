package com.example.wac_money.model

import java.util.Date

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Date,
    val note: String? = null
)

enum class TransactionType {
    INCOME,
    EXPENSE
}
