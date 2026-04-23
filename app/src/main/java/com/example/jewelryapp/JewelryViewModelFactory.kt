package com.example.jewelryapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jewelryapp.data.JewelryRepository

class JewelryViewModelFactory(
    private val repository: JewelryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JewelryViewModel::class.java)) {
            return JewelryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}