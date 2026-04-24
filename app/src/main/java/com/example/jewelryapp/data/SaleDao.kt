package com.example.jewelryapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert
    suspend fun insertSaleMaterialRefs(refs: List<SaleMaterialCrossRef>)

    @Insert
    suspend fun insertSaleExpenses(expenses: List<SaleExpenseEntity>)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Query("DELETE FROM sale_material_cross_ref WHERE saleId = :saleId")
    suspend fun deleteSaleMaterialRefs(saleId: Int)

    @Query("DELETE FROM sale_expenses WHERE saleId = :saleId")
    suspend fun deleteSaleExpenses(saleId: Int)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("SELECT * FROM sale_material_cross_ref")
    fun getAllCrossRefs(): Flow<List<SaleMaterialCrossRef>>

    @Query("SELECT * FROM sale_expenses")
    fun getAllSaleExpenses(): Flow<List<SaleExpenseEntity>>

    @Transaction
    @Query("SELECT * FROM sales ORDER BY id DESC")
    fun getAllSalesRaw(): Flow<List<SaleWithMaterialsRoom>>
}
