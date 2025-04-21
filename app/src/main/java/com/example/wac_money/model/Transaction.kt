package com.example.wac_money.model

import java.util.Date

data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: TransactionCategory,
    val date: Date,
    val type: TransactionType
)

enum class TransactionCategory {
    FOOD,
    RENT,
    SALARY,
    TRANSPORTATION,
    ENTERTAINMENT,
    UTILITIES,
    OTHER
}

enum class TransactionType {
    INCOME,
    EXPENSE
}
