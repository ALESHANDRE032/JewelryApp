package com.example.jewelryapp.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.MaterialEntity
import com.example.jewelryapp.data.SaleWithMaterials
import com.example.jewelryapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleBottomSheet(
    materials: List<MaterialEntity>,
    initialSale: SaleWithMaterials? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, List<MaterialEntity>) -> Unit
) {
    var name    by remember { mutableStateOf(initialSale?.sale?.name ?: "") }
    var price   by remember { mutableStateOf(if (initialSale != null) "${initialSale.sale.salePrice}" else "") }
    var channel by remember { mutableStateOf(initialSale?.sale?.channel ?: "") }

    val selectedMaterials = remember {
        mutableStateListOf<MaterialEntity>().also { list ->
            initialSale?.materials?.let { list.addAll(it) }
        }
    }

    val costSum  = selectedMaterials.sumOf { it.cost }
    val priceInt = price.replace(',', '.').toDoubleOrNull()?.toInt() ?: price.toIntOrNull() ?: 0
    val profit   = priceInt - costSum
    val isProfit = profit >= 0
    val isValid  = name.isNotBlank() && price.isNotBlank() && channel.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (initialSale != null) "Редактировать продажу" else "Новая продажа",
                style = MaterialTheme.typography.displaySmall,
                color = Ink
            )

            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { input ->
                    name = input.replaceFirstChar { it.uppercase() }
                },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors()
            )
            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == ',' }) price = input
                },
                label = { Text("Цена продажи, ₽") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = channel,
                onValueChange = { input ->
                    channel = input.replaceFirstChar { it.uppercase() }
                },
                label = { Text("Канал продажи") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors()
            )

            if (materials.isNotEmpty()) {
                Text("Выбери материалы:", style = MaterialTheme.typography.titleMedium, color = Ink)

                LazyColumn(modifier = Modifier.height(160.dp)) {
                    items(materials) { material ->
                        val isSelected = selectedMaterials.any { it.id == material.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(value = isSelected, onValueChange = {
                                    if (isSelected) selectedMaterials.removeAll { it.id == material.id }
                                    else selectedMaterials.add(material)
                                })
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor   = Gold,
                                    uncheckedColor = Divider
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${material.name} · ${material.cost} ₽",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Ink
                            )
                        }
                    }
                }
            }

            Surface(
                color = if (isProfit) ProfitBg else LossBg,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("СЕБЕСТОИМОСТЬ", style = Eyebrow, color = Muted)
                        Text("$costSum ₽", style = NumberLg, color = Ink)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ПРИБЫЛЬ", style = Eyebrow, color = Muted)
                        Text(
                            "$profit ₽",
                            style = NumberLg,
                            color = if (isProfit) Profit else Loss
                        )
                    }
                }
            }

            Button(
                onClick = { onConfirm(name.trimEnd(), priceInt, channel.trimEnd(), selectedMaterials.toList()) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = Ink,
                    contentColor           = Surface,
                    disabledContainerColor = SurfaceDeep,
                    disabledContentColor   = Muted
                )
            ) {
                Text(
                    if (initialSale != null) "Сохранить" else "Добавить продажу",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Gold,
    unfocusedBorderColor    = Divider,
    focusedContainerColor   = GoldBg,
    unfocusedContainerColor = SurfaceAlt
)
