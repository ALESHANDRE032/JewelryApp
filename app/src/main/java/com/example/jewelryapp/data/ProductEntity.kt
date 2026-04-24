package com.example.jewelryapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val expectedSalePrice: Int,
    val expectedCost: Int,
    val quantity: Int = 0
)
