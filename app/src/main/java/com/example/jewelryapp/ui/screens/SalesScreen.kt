package com.example.jewelryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.SaleWithMaterials
import com.example.jewelryapp.ui.components.SaleCard
import com.example.jewelryapp.ui.theme.*

@Composable
fun SalesScreen(
    sales: List<SaleWithMaterials>,
    onDeleteSale: (SaleWithMaterials) -> Unit,
    onEditSale: (SaleWithMaterials) -> Unit,
    onAddSale: () -> Unit
) {
    val channels = listOf("Все") + sales.map { it.sale.channel }.distinct()
    var selectedChannel by remember { mutableStateOf("Все") }

    val filtered = if (selectedChannel == "Все") sales
                   else sales.filter { it.sale.channel == selectedChannel }

    val totalRevenue = sales.sumOf { it.sale.salePrice }
    val totalProfit  = sales.sumOf { it.sale.profit }
    val avgCheck     = if (sales.isNotEmpty()) totalRevenue / sales.size else 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddSale,
                containerColor = Ink,
                contentColor = Surface,
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("+ Продажа", style = MaterialTheme.typography.labelLarge)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp)
                ) {
                    Text("${sales.size} записей", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    Text("Продажи", style = MaterialTheme.typography.displaySmall, color = Ink)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatChip("Выручка", "$totalRevenue ₽", Modifier.weight(1f))
                        StatChip("Прибыль", "$totalProfit ₽", Modifier.weight(1f))
                        StatChip("Средний чек", "$avgCheck ₽", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(channels) { channel ->
                        FilterChip(
                            selected = channel == selectedChannel,
                            onClick  = { selectedChannel = channel },
                            label    = { Text(channel, style = MaterialTheme.typography.bodyLarge) },
                            shape    = RoundedCornerShape(100.dp),
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Ink,
                                selectedLabelColor     = Surface,
                                containerColor         = MaterialTheme.colorScheme.background,
                                labelColor             = Ink
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = channel == selectedChannel,
                                borderColor = Divider, selectedBorderColor = Ink
                            )
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            items(filtered) { sale ->
                Box(Modifier.padding(horizontal = 20.dp)) {
                    SaleCard(
                        sale     = sale,
                        onDelete = { onDeleteSale(sale) },
                        onEdit   = { onEditSale(sale) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = SurfaceAlt,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Muted)
            Spacer(Modifier.height(2.dp))
            Text(value, style = NumberLg, color = Ink)
        }
    }
}
