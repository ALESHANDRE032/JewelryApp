package com.example.jewelryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jewelryapp.data.AppDatabase
import com.example.jewelryapp.data.JewelryRepository
import com.example.jewelryapp.ui.screens.JewelryAppScreen
import com.example.jewelryapp.ui.theme.JewelryAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = JewelryRepository(
            materialDao = database.materialDao(),
            saleDao = database.saleDao()
        )
        val factory = JewelryViewModelFactory(repository)

        setContent {
            val viewModel: JewelryViewModel = viewModel(factory = factory)
            JewelryAppTheme {
                JewelryAppScreen(viewModel = viewModel)
            }
        }
    }
}
