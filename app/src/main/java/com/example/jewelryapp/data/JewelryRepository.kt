package com.example.jewelryapp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class JewelryRepository(
    private val materialDao: MaterialDao,
    private val saleDao: SaleDao,
    private val productDao: ProductDao
) {

    // ── Materials ──────────────────────────────────────────────────────────────

    fun getAllMaterials(): Flow<List<MaterialEntity>> = materialDao.getAllMaterials()

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

    // ── Products ───────────────────────────────────────────────────────────────

    fun getAllProductsWithMaterials(): Flow<List<ProductWithMaterials>> =
        productDao.getAllProductsRaw().combine(productDao.getAllProductCrossRefs()) { productsRaw, crossRefs ->
            val refMap = crossRefs
                .groupBy { it.productId }
                .mapValues { (_, refs) -> refs.associate { it.materialId to it.usedQuantity } }
            productsRaw.map { raw ->
                val quantities = refMap[raw.product.id] ?: emptyMap()
                ProductWithMaterials(
                    product = raw.product,
                    materials = raw.materials.map { material ->
                        MaterialWithUsage(material, quantities[material.id] ?: 1.0)
                    }
                )
            }
        }

    // Returns null on success, error message on failure
    suspend fun addProduct(
        name: String,
        expectedSalePrice: Int,
        materials: List<MaterialWithUsage>
    ): String? {
        for (usage in materials) {
            val current = materialDao.getMaterialById(usage.material.id) ?: continue
            if (current.quantity < usage.usedQuantity) {
                return "Недостаточно: ${current.name} (есть ${formatQty(current.quantity)} ${current.unit})"
            }
        }
        val expectedCost = materials.sumOf { it.material.unitCost * it.usedQuantity }.toInt()
        val productId = productDao.insertProduct(
            ProductEntity(name = name, expectedSalePrice = expectedSalePrice, expectedCost = expectedCost, quantity = 1)
        ).toInt()
        if (materials.isNotEmpty()) {
            productDao.insertProductMaterialRefs(
                materials.map {
                    ProductMaterialCrossRef(productId = productId, materialId = it.material.id, usedQuantity = it.usedQuantity)
                }
            )
        }
        materials.forEach { usage ->
            val current = materialDao.getMaterialById(usage.material.id) ?: return@forEach
            materialDao.updateMaterial(current.copy(quantity = current.quantity - usage.usedQuantity))
        }
        return null
    }

    // Returns null on success, error message on failure.
    // Always restores old materials and deducts new ones regardless of product.quantity.
    suspend fun updateProduct(
        productId: Int,
        name: String,
        expectedSalePrice: Int,
        oldMaterials: List<MaterialWithUsage>,
        newMaterials: List<MaterialWithUsage>
    ): String? {
        // Restore old materials
        oldMaterials.forEach { usage ->
            val current = materialDao.getMaterialById(usage.material.id) ?: return@forEach
            materialDao.updateMaterial(current.copy(quantity = current.quantity + usage.usedQuantity))
        }
        // Validate new materials
        for (usage in newMaterials) {
            val current = materialDao.getMaterialById(usage.material.id) ?: continue
            if (current.quantity < usage.usedQuantity) {
                // Rollback restore
                oldMaterials.forEach { old ->
                    val cur = materialDao.getMaterialById(old.material.id) ?: return@forEach
                    materialDao.updateMaterial(cur.copy(quantity = cur.quantity - old.usedQuantity))
                }
                return "Недостаточно: ${current.name} (есть ${formatQty(current.quantity)} ${current.unit})"
            }
        }
        val expectedCost = newMaterials.sumOf { it.material.unitCost * it.usedQuantity }.toInt()
        val product = productDao.getProductById(productId) ?: return "Товар не найден"
        productDao.updateProduct(
            product.copy(name = name, expectedSalePrice = expectedSalePrice, expectedCost = expectedCost)
        )
        productDao.deleteProductMaterialRefs(productId)
        if (newMaterials.isNotEmpty()) {
            productDao.insertProductMaterialRefs(
                newMaterials.map {
                    ProductMaterialCrossRef(productId = productId, materialId = it.material.id, usedQuantity = it.usedQuantity)
                }
            )
        }
        newMaterials.forEach { usage ->
            val current = materialDao.getMaterialById(usage.material.id) ?: return@forEach
            materialDao.updateMaterial(current.copy(quantity = current.quantity - usage.usedQuantity))
        }
        return null
    }

    // Restores materials only for unsold units (quantity > 0)
    suspend fun deleteProduct(product: ProductWithMaterials) {
        if (product.product.quantity > 0) {
            product.materials.forEach { usage ->
                val current = materialDao.getMaterialById(usage.material.id) ?: return@forEach
                materialDao.updateMaterial(
                    current.copy(quantity = current.quantity + usage.usedQuantity * product.product.quantity)
                )
            }
        }
        productDao.deleteProduct(product.product)
    }

    // ── Sales ──────────────────────────────────────────────────────────────────

    fun getAllSalesWithMaterials(): Flow<List<SaleWithMaterials>> =
        combine(
            saleDao.getAllSalesRaw(),
            saleDao.getAllCrossRefs(),
            saleDao.getAllSaleExpenses()
        ) { salesRaw, crossRefs, expenses ->
            val refMap = crossRefs
                .groupBy { it.saleId }
                .mapValues { (_, refs) -> refs.associate { it.materialId to it.usedQuantity } }
            val expenseMap = expenses.groupBy { it.saleId }
            salesRaw.map { raw ->
                val quantities = refMap[raw.sale.id] ?: emptyMap()
                SaleWithMaterials(
                    sale = raw.sale,
                    materials = raw.materials.map { material ->
                        MaterialWithUsage(material, quantities[material.id] ?: 1.0)
                    },
                    expenses = expenseMap[raw.sale.id] ?: emptyList()
                )
            }
        }

    // Returns null on success, error message on failure
    suspend fun addSale(
        name: String,
        salePrice: Int,
        channel: String,
        productId: Int?,
        expenses: List<SaleExpenseEntity>
    ): String? {
        var productCost = 0
        if (productId != null) {
            val product = productDao.getProductById(productId) ?: return "Товар не найден"
            if (product.quantity <= 0) return "Товар закончился: ${product.name}"
            productCost = product.expectedCost
        }
        val extraExpensesCost = expenses.sumOf { it.amount }
        val profit = salePrice - productCost - extraExpensesCost
        val saleId = saleDao.insertSale(
            SaleEntity(
                name = name,
                salePrice = salePrice,
                channel = channel,
                profit = profit,
                productId = productId,
                extraExpensesCost = extraExpensesCost
            )
        ).toInt()
        if (expenses.isNotEmpty()) {
            saleDao.insertSaleExpenses(expenses.map { it.copy(saleId = saleId) })
        }
        if (productId != null) {
            val product = productDao.getProductById(productId)
            if (product != null) {
                productDao.updateProduct(product.copy(quantity = product.quantity - 1))
            }
        }
        return null
    }

    // Returns null on success, error message on failure.
    // Restores old product qty (and legacy material refs), validates new, rolls back on failure.
    suspend fun updateSale(
        saleId: Int,
        name: String,
        salePrice: Int,
        channel: String,
        oldSale: SaleWithMaterials,
        newProductId: Int?,
        expenses: List<SaleExpenseEntity>
    ): String? {
        val oldProductId = oldSale.sale.productId

        // Restore old product quantity
        if (oldProductId != null) {
            val oldProduct = productDao.getProductById(oldProductId)
            if (oldProduct != null) {
                productDao.updateProduct(oldProduct.copy(quantity = oldProduct.quantity + 1))
            }
        }
        // Restore legacy material refs if any (converts old-format sale to new format)
        if (oldSale.materials.isNotEmpty()) {
            oldSale.materials.forEach { usage ->
                val current = materialDao.getMaterialById(usage.material.id) ?: return@forEach
                materialDao.updateMaterial(current.copy(quantity = current.quantity + usage.usedQuantity))
            }
            saleDao.deleteSaleMaterialRefs(saleId)
        }

        // Validate new product
        var productCost = 0
        if (newProductId != null) {
            val newProduct = productDao.getProductById(newProductId)
            if (newProduct == null) {
                rollbackSaleUpdate(oldProductId, oldSale)
                return "Товар не найден"
            }
            // Allow same product (qty was just restored above)
            if (newProduct.quantity <= 0) {
                rollbackSaleUpdate(oldProductId, oldSale)
                return "Товар закончился: ${newProduct.name}"
            }
            productCost = newProduct.expectedCost
        }

        val extraExpensesCost = expenses.sumOf { it.amount }
        val profit = salePrice - productCost - extraExpensesCost
        saleDao.updateSale(
            SaleEntity(
                id = saleId,
                name = name,
                salePrice = salePrice,
                channel = channel,
                profit = profit,
                productId = newProductId,
                extraExpensesCost = extraExpensesCost
            )
        )
        saleDao.deleteSaleExpenses(saleId)
        if (expenses.isNotEmpty()) {
            saleDao.insertSaleExpenses(expenses.map { it.copy(saleId = saleId) })
        }

        // Deduct new product quantity
        if (newProductId != null) {
            val newProduct = productDao.getProductById(newProductId)
            if (newProduct != null) {
                productDao.updateProduct(newProduct.copy(quantity = newProduct.quantity - 1))
            }
        }
        return null
    }

    private suspend fun rollbackSaleUpdate(oldProductId: Int?, oldSale: SaleWithMaterials) {
        if (oldProductId != null) {
            val p = productDao.getProductById(oldProductId)
            if (p != null) productDao.updateProduct(p.copy(quantity = p.quantity - 1))
        }
        if (oldSale.materials.isNotEmpty()) {
            oldSale.materials.forEach { usage ->
                val cur = materialDao.getMaterialById(usage.material.id) ?: return@forEach
                materialDao.updateMaterial(cur.copy(quantity = cur.quantity - usage.usedQuantity))
            }
        }
    }

    suspend fun deleteSale(sale: SaleWithMaterials) {
        // Restore legacy material stock
        sale.materials.forEach { usage ->
            val current = materialDao.getMaterialById(usage.material.id) ?: return@forEach
            materialDao.updateMaterial(current.copy(quantity = current.quantity + usage.usedQuantity))
        }
        // Restore product quantity
        val productId = sale.sale.productId
        if (productId != null) {
            val product = productDao.getProductById(productId)
            if (product != null) {
                productDao.updateProduct(product.copy(quantity = product.quantity + 1))
            }
        }
        saleDao.deleteSale(sale.sale)
    }

    private fun formatQty(qty: Double): String =
        if (qty == qty.toLong().toDouble()) qty.toLong().toString() else "%.2f".format(qty)
}
