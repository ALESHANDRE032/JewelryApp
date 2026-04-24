package com.example.jewelryapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jewelryapp.data.JewelryRepository
import com.example.jewelryapp.data.MaterialEntity
import com.example.jewelryapp.data.MaterialWithUsage
import com.example.jewelryapp.data.ProductWithMaterials
import com.example.jewelryapp.data.SaleExpenseEntity
import com.example.jewelryapp.data.SaleWithMaterials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JewelryViewModel(
    private val repository: JewelryRepository
) : ViewModel() {

    val materials = repository
        .getAllMaterials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sales = repository
        .getAllSalesWithMaterials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val products = repository
        .getAllProductsWithMaterials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() { _error.value = null }

    // ── Material actions ───────────────────────────────────────────────────────

    fun addMaterial(name: String, cost: Int, quantity: Double, unit: String) {
        viewModelScope.launch { repository.addMaterial(name, cost, quantity, unit) }
    }

    fun updateMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.updateMaterial(material) }
    }

    fun deleteMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.deleteMaterial(material) }
    }

    // ── Product actions ────────────────────────────────────────────────────────

    fun addProduct(name: String, expectedSalePrice: Int, materials: List<MaterialWithUsage>) {
        viewModelScope.launch {
            val err = repository.addProduct(name, expectedSalePrice, materials)
            if (err != null) _error.value = err
        }
    }

    fun updateProduct(
        productId: Int,
        name: String,
        expectedSalePrice: Int,
        oldMaterials: List<MaterialWithUsage>,
        newMaterials: List<MaterialWithUsage>
    ) {
        viewModelScope.launch {
            val err = repository.updateProduct(productId, name, expectedSalePrice, oldMaterials, newMaterials)
            if (err != null) _error.value = err
        }
    }

    fun deleteProduct(product: ProductWithMaterials) {
        viewModelScope.launch { repository.deleteProduct(product) }
    }

    // ── Sale actions ───────────────────────────────────────────────────────────

    fun addSale(
        name: String,
        salePrice: Int,
        channel: String,
        productId: Int?,
        expenses: List<SaleExpenseEntity>
    ) {
        viewModelScope.launch {
            val err = repository.addSale(name, salePrice, channel, productId, expenses)
            if (err != null) _error.value = err
        }
    }

    fun updateSale(
        saleId: Int,
        name: String,
        salePrice: Int,
        channel: String,
        oldSale: SaleWithMaterials,
        newProductId: Int?,
        expenses: List<SaleExpenseEntity>
    ) {
        viewModelScope.launch {
            val err = repository.updateSale(saleId, name, salePrice, channel, oldSale, newProductId, expenses)
            if (err != null) _error.value = err
        }
    }

    fun deleteSale(sale: SaleWithMaterials) {
        viewModelScope.launch { repository.deleteSale(sale) }
    }
}
