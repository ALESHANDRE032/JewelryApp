package com.example.jewelryapp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class JewelryRepository(
    private val materialDao: MaterialDao,
    private val saleDao: SaleDao
) {
    fun getAllMaterials(): Flow<List<MaterialEntity>> = materialDao.getAllMaterials()

    fun getAllSalesWithMaterials(): Flow<List<SaleWithMaterials>> =
        saleDao.getAllSalesRaw().combine(saleDao.getAllCrossRefs()) { salesRaw, crossRefs ->
            val refMap = crossRefs
                .groupBy { it.saleId }
                .mapValues { (_, refs) -> refs.associate { it.materialId to it.usedQuantity } }
            salesRaw.map { raw ->
                val quantities = refMap[raw.sale.id] ?: emptyMap()
                SaleWithMaterials(
                    sale = raw.sale,
                    materials = raw.materials.map { material ->
                        MaterialWithUsage(material, quantities[material.id] ?: 1.0)
                    }
                )
            }
        }

    suspend fun addMaterial(name: String, cost: Int, quantity: Double, unit: String) {
        val unitCost = if (quantity > 0) cost.toDouble() / quantity else 0.0
        materialDao.insertMaterial(
            MaterialEntity(name = name, cost = cost, quantity = quantity, unit = unit, unitCost = unitCost)
        )
    }

    suspend fun updateMaterial(material: MaterialEntity) {
        val unitCost = if (material.quantity > 0) material.cost.toDouble() / material.quantity else 0.0
        materialDao.updateMaterial(material.copy(unitCost = unitCost))
    }

    suspend fun deleteMaterial(material: MaterialEntity) {
        materialDao.deleteMaterial(material)
    }

    suspend fun deleteSale(sale: SaleWithMaterials) {
        sale.materials.forEach { usage ->
            val current = materialDao.getMaterialById(usage.material.id) ?: return@forEach
            materialDao.updateMaterial(current.copy(quantity = current.quantity + usage.usedQuantity))
        }
        saleDao.deleteSale(sale.sale)
    }

    // Returns null on success, or an error message on stock validation failure
    suspend fun addSale(
        name: String,
        salePrice: Int,
        channel: String,
        materials: List<MaterialWithUsage>
    ): String? {
        for (usage in materials) {
            val current = materialDao.getMaterialById(usage.material.id) ?: continue
            if (current.quantity < usage.usedQuantity) {
                return "Недостаточно: ${current.name} (есть ${formatQty(current.quantity)} ${current.unit})"
            }
        }
        val cost = materials.sumOf { it.material.unitCost * it.usedQuantity }.toInt()
        val profit = salePrice - cost
        val saleId = saleDao.insertSale(
            SaleEntity(name = name, salePrice = salePrice, channel = channel, profit = profit)
        ).toInt()
        if (materials.isNotEmpty()) {
            saleDao.insertSaleMaterialRefs(
                materials.map {
                    SaleMaterialCrossRef(saleId = saleId, materialId = it.material.id, usedQuantity = it.usedQuantity)
                }
            )
        }
        materials.forEach { usage ->
            val current = materialDao.getMaterialById(usage.material.id) ?: return@forEach
            materialDao.updateMaterial(current.copy(quantity = current.quantity - usage.usedQuantity))
        }
        return null
    }

    // Returns null on success, or an error message on stock validation failure
    suspend fun updateSale(
        saleId: Int,
        name: String,
        salePrice: Int,
        channel: String,
        oldMaterials: List<MaterialWithUsage>,
        newMaterials: List<MaterialWithUsage>
    ): String? {
        // Restore old quantities so validation sees the full available stock
        oldMaterials.forEach { usage ->
            val current = materialDao.getMaterialById(usage.material.id) ?: return@forEach
            materialDao.updateMaterial(current.copy(quantity = current.quantity + usage.usedQuantity))
        }
        // Validate new quantities
        for (usage in newMaterials) {
            val current = materialDao.getMaterialById(usage.material.id) ?: continue
            if (current.quantity < usage.usedQuantity) {
                // Roll back the restore
                oldMaterials.forEach { old ->
                    val cur = materialDao.getMaterialById(old.material.id) ?: return@forEach
                    materialDao.updateMaterial(cur.copy(quantity = cur.quantity - old.usedQuantity))
                }
                return "Недостаточно: ${current.name} (есть ${formatQty(current.quantity)} ${current.unit})"
            }
        }
        val cost = newMaterials.sumOf { it.material.unitCost * it.usedQuantity }.toInt()
        val profit = salePrice - cost
        saleDao.updateSale(
            SaleEntity(id = saleId, name = name, salePrice = salePrice, channel = channel, profit = profit)
        )
        saleDao.deleteSaleMaterialRefs(saleId)
        if (newMaterials.isNotEmpty()) {
            saleDao.insertSaleMaterialRefs(
                newMaterials.map {
                    SaleMaterialCrossRef(saleId = saleId, materialId = it.material.id, usedQuantity = it.usedQuantity)
                }
            )
        }
        newMaterials.forEach { usage ->
            val current = materialDao.getMaterialById(usage.material.id) ?: return@forEach
            materialDao.updateMaterial(current.copy(quantity = current.quantity - usage.usedQuantity))
        }
        return null
    }

    private fun formatQty(qty: Double): String =
        if (qty == qty.toLong().toDouble()) qty.toLong().toString() else "%.2f".format(qty)
}
