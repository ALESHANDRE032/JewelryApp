package com.example.jewelryapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Insert
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Insert
    suspend fun insertProductMaterialRefs(refs: List<ProductMaterialCrossRef>)

    @Query("DELETE FROM product_material_cross_ref WHERE productId = :productId")
    suspend fun deleteProductMaterialRefs(productId: Int)

    @Query("SELECT * FROM product_material_cross_ref")
    fun getAllProductCrossRefs(): Flow<List<ProductMaterialCrossRef>>

    @Transaction
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProductsRaw(): Flow<List<ProductWithMaterialsRoom>>
}
