package com.example.jewelryapp.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.MaterialEntity

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

    val isValid = name.isNotBlank() && price.isNotBlank() && channel.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(name, priceInt, channel, selectedMaterials.toList()) },
                enabled = isValid
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Отмена") }
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

                LazyColumn(modifier = Modifier.height(150.dp)) {
                    items(materials) { material ->
                        val isSelected = selectedMaterials.contains(material)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = isSelected,
                                    onValueChange = {
                                        if (isSelected) selectedMaterials.remove(material)
                                        else selectedMaterials.add(material)
                                    }
                                )
                                .padding(8.dp)
                        ) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
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
