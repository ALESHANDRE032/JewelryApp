package com.example.jewelryapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_expenses",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId")]
)
data class SaleExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val saleId: Int,
    val expenseType: String,
    val amount: Int
)
