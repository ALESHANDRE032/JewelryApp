package com.example.jewelryapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {

    @Query("SELECT * FROM materials ORDER BY id DESC")
    fun getAllMaterials(): Flow<List<MaterialEntity>>

    @Insert
    suspend fun insertMaterial(material: MaterialEntity)
}