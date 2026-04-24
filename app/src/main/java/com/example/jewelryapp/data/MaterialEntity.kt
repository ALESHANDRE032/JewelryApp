package com.example.jewelryapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val cost: Int,
    val quantity: Double = 1.0,
    val unit: String = "шт",
    val unitCost: Double = 0.0
)
