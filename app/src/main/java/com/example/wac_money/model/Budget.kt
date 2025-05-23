package com.example.wac_money.model

data class Budget(
    val id: Long = 0,
    val amount: Double,
    val category: String? = null,
    val month: Int,
    val year: Int
)
