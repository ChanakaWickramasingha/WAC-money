package com.example.wac_money.data

import java.util.Date

data class Budget(
    val id: Long = 0,
    val amount: Double,
    val month: Int,
    val year: Int,
    val createdAt: Date = Date()
)
