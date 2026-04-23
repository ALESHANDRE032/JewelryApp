package com.example.jewelryapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jewelryapp.data.JewelryRepository
import com.example.jewelryapp.data.MaterialEntity
import com.example.jewelryapp.data.SaleWithMaterials
import kotlinx.coroutines.flow.SharingStarted
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

    fun addMaterial(name: String, cost: Int) {
        viewModelScope.launch {
            repository.addMaterial(name, cost)
        }
    }

    fun deleteSale(sale: SaleWithMaterials) {
        viewModelScope.launch {
            repository.deleteSale(sale)
        }
    }

    fun addSale(
        name: String,
        salePrice: Int,
        channel: String,
        selectedMaterials: List<MaterialEntity>
    ) {
        viewModelScope.launch {
            repository.addSale(
                name = name,
                salePrice = salePrice,
                channel = channel,
                selectedMaterials = selectedMaterials
            )
        }
    }
}