package com.example.jewelryapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val salePrice: Int,
    val channel: String,
    val profit: Int,
    val productId: Int? = null,
    val extraExpensesCost: Int = 0,
    val comment: String = "",
    val saleDate: Long = 0L
)
