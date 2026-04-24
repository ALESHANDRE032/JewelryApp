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
        materialDao.insertMaterial(MaterialEntity(name = name, cost = cost))
    }

    suspend fun updateMaterial(material: MaterialEntity) {
        materialDao.updateMaterial(material)
    }

    suspend fun deleteMaterial(material: MaterialEntity) {
        materialDao.deleteMaterial(material)
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
            SaleEntity(name = name, salePrice = salePrice, channel = channel, profit = profit)
        ).toInt()
        saleDao.insertSaleMaterialRefs(
            selectedMaterials.map { SaleMaterialCrossRef(saleId = saleId, materialId = it.id) }
        )
    }

    suspend fun updateSale(
        saleId: Int,
        name: String,
        salePrice: Int,
        channel: String,
        selectedMaterials: List<MaterialEntity>
    ) {
        val profit = salePrice - selectedMaterials.sumOf { it.cost }
        saleDao.updateSale(
            SaleEntity(id = saleId, name = name, salePrice = salePrice, channel = channel, profit = profit)
        )
        saleDao.deleteSaleMaterialRefs(saleId)
        if (selectedMaterials.isNotEmpty()) {
            saleDao.insertSaleMaterialRefs(
                selectedMaterials.map { SaleMaterialCrossRef(saleId = saleId, materialId = it.id) }
            )
        }
    }
}
