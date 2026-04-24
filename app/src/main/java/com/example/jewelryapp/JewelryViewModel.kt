package com.example.jewelryapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jewelryapp.data.JewelryRepository
import com.example.jewelryapp.data.MaterialEntity
import com.example.jewelryapp.data.MaterialWithUsage
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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() { _error.value = null }

    fun addMaterial(name: String, cost: Int, quantity: Double, unit: String) {
        viewModelScope.launch { repository.addMaterial(name, cost, quantity, unit) }
    }

    fun updateMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.updateMaterial(material) }
    }

    fun deleteMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.deleteMaterial(material) }
    }

    fun deleteSale(sale: SaleWithMaterials) {
        viewModelScope.launch { repository.deleteSale(sale) }
    }

    fun addSale(name: String, salePrice: Int, channel: String, materials: List<MaterialWithUsage>) {
        viewModelScope.launch {
            val err = repository.addSale(name, salePrice, channel, materials)
            if (err != null) _error.value = err
        }
    }

    fun updateSale(
        saleId: Int,
        name: String,
        salePrice: Int,
        channel: String,
        oldMaterials: List<MaterialWithUsage>,
        newMaterials: List<MaterialWithUsage>
    ) {
        viewModelScope.launch {
            val err = repository.updateSale(saleId, name, salePrice, channel, oldMaterials, newMaterials)
            if (err != null) _error.value = err
        }
    }
}
