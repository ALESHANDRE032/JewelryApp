package com.example.jewelryapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jewelryapp.data.AppDatabase
import com.example.jewelryapp.data.JewelryRepository
import com.example.jewelryapp.data.MaterialEntity
import com.example.jewelryapp.data.SaleWithMaterials
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JewelryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = JewelryRepository(
        materialDao = database.materialDao(),
        saleDao = database.saleDao()
    )

    val materials = repository
        .getAllMaterials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val sales = repository
        .getAllSalesWithMaterials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addMaterial(name: String, cost: Int) {
        viewModelScope.launch {
            repository.addMaterial(name, cost)
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JewelryAppScreen()
        }
    }
}

@Composable
fun JewelryAppScreen(viewModel: JewelryViewModel = viewModel()) {
    var showSaleDialog by remember { mutableStateOf(false) }
    var showMaterialDialog by remember { mutableStateOf(false) }

    val materials by viewModel.materials.collectAsState()
    val sales by viewModel.sales.collectAsState()

    val totalProfit = sales.sumOf { it.sale.profit }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Юнит-экономика украшений",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showMaterialDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить материал")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showSaleDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить продажу")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Материалов: ${materials.size}",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Общая прибыль: $totalProfit ₽",
            style = MaterialTheme.typography.bodyLarge,
            color = if (totalProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "История продаж",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sales) { sale ->
                SaleCard(sale)
            }
        }
    }

    if (showSaleDialog) {
        AddSaleDialog(
            materials = materials,
            onDismiss = { showSaleDialog = false },
            onConfirm = { name, price, channel, selectedMaterials ->
                viewModel.addSale(
                    name = name,
                    salePrice = price,
                    channel = channel,
                    selectedMaterials = selectedMaterials
                )
                showSaleDialog = false
            }
        )
    }

    if (showMaterialDialog) {
        AddMaterialDialog(
            onDismiss = { showMaterialDialog = false },
            onConfirm = { name, cost ->
                viewModel.addMaterial(name, cost)
                showMaterialDialog = false
            }
        )
    }
}

@Composable
fun AddSaleDialog(
    materials: List<MaterialEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, List<MaterialEntity>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf("") }

    val selectedMaterials = remember { mutableStateListOf<MaterialEntity>() }

    val costSum = selectedMaterials.sumOf { it.cost }
    val priceInt = price.toIntOrNull() ?: 0
    val profit = priceInt - costSum

    val isValid = name.isNotBlank() &&
            price.isNotBlank() &&
            channel.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name,
                        priceInt,
                        channel,
                        selectedMaterials.toList()
                    )
                },
                enabled = isValid
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        title = { Text("Новая продажа") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Цена продажи") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = channel,
                    onValueChange = { channel = it },
                    label = { Text("Канал") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Выбери материалы:")

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(150.dp)
                ) {
                    items(materials) { material ->
                        val isSelected = selectedMaterials.contains(material)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = isSelected,
                                    onValueChange = {
                                        if (isSelected) {
                                            selectedMaterials.remove(material)
                                        } else {
                                            selectedMaterials.add(material)
                                        }
                                    }
                                )
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${material.name} (${material.cost} ₽)")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Себестоимость: $costSum ₽")
                Text(
                    text = "Прибыль: $profit ₽",
                    color = if (profit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }
    )
}

@Composable
fun AddMaterialDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() && cost.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name, cost.toIntOrNull() ?: 0)
                },
                enabled = isValid
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        title = { Text("Новый материал") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Стоимость") }
                )
            }
        }
    )
}

@Composable
fun SaleCard(sale: SaleWithMaterials) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = sale.sale.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Цена: ${sale.sale.salePrice} ₽")
            Text("Канал: ${sale.sale.channel}")

            Spacer(modifier = Modifier.height(4.dp))

            if (sale.materials.isNotEmpty()) {
                Text("Материалы:")
                sale.materials.forEach {
                    Text("- ${it.name} (${it.cost} ₽)")
                }
            } else {
                Text("Материалы: не выбраны")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Прибыль: ${sale.sale.profit} ₽",
                color = if (sale.sale.profit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}