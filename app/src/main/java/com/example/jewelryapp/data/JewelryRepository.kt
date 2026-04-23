package com.example.jewelryapp.data

import kotlinx.coroutines.flow.Flow

class JewelryRepository(
    private val materialDao: MaterialDao,
    private val saleDao: SaleDao
) {
    fun getAllMaterials(): Flow<List<MaterialEntity>> = materialDao.getAllMaterials()

    fun getAllSalesWithMaterials(): Flow<List<SaleWithMaterials>> =
        saleDao.getAllSalesWithMaterials()

    suspend fun addMaterial(name: String, cost: Int) {
        materialDao.insertMaterial(
            MaterialEntity(
                name = name,
                cost = cost
            )
        )
    }

    suspend fun deleteSale(sale: SaleWithMaterials) {
        saleDao.deleteSale(sale.sale)
    }

    suspend fun addSale(
        name: String,
        salePrice: Int,
        channel: String,
        selectedMaterials: List<MaterialEntity>
    ) {
        val profit = salePrice - selectedMaterials.sumOf { it.cost }

        val saleId = saleDao.insertSale(
            SaleEntity(
                name = name,
                salePrice = salePrice,
                channel = channel,
                profit = profit
            )
        ).toInt()

        val refs = selectedMaterials.map { material ->
            SaleMaterialCrossRef(
                saleId = saleId,
                materialId = material.id
            )
        }

        saleDao.insertSaleMaterialRefs(refs)
    }
}