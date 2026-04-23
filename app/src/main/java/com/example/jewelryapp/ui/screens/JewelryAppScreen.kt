package com.example.jewelryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.JewelryViewModel
import com.example.jewelryapp.ui.components.SaleCard
import com.example.jewelryapp.ui.dialogs.AddMaterialDialog
import com.example.jewelryapp.ui.dialogs.AddSaleDialog

@Composable
fun JewelryAppScreen(viewModel: JewelryViewModel) {
    var showSaleDialog by remember { mutableStateOf(false) }
    var showMaterialDialog by remember { mutableStateOf(false) }

    val sales by viewModel.sales.collectAsState()
    val materials by viewModel.materials.collectAsState()

    val totalProfit = sales.sumOf { it.sale.profit }
    val salesCount = sales.size
    val averageProfit = if (salesCount > 0) totalProfit / salesCount else 0

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

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Аналитика", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Материалов: ${materials.size}")
                Text("Продаж: $salesCount")
                Text(
                    text = "Общая прибыль: $totalProfit ₽",
                    color = if (totalProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Text(
                    text = "Средняя прибыль: $averageProfit ₽",
                    color = if (averageProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "История продаж",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sales) { sale ->
                SaleCard(
                    sale = sale,
                    onDelete = { viewModel.deleteSale(sale) }
                )
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
