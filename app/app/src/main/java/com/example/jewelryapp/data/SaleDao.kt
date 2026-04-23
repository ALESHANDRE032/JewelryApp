package com.example.jewelryapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert
    suspend fun insertSaleMaterialRefs(refs: List<SaleMaterialCrossRef>)

    @Transaction
    @Query("SELECT * FROM sales ORDER BY id DESC")
    fun getAllSalesWithMaterials(): Flow<List<SaleWithMaterials>>
}