package com.example.jewelryapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.SaleWithMaterials

@Composable
fun SaleCard(sale: SaleWithMaterials, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sale.sale.name,
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onDelete) {
                    Text("Удалить", color = Color(0xFFC62828))
                }
            }

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
