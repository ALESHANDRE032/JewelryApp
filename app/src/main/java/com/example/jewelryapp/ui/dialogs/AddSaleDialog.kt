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
import com.example.jewelryapp.data.MaterialWithUsage
import com.example.jewelryapp.data.SaleWithMaterials
import com.example.jewelryapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleBottomSheet(
    materials: List<MaterialEntity>,
    initialSale: SaleWithMaterials? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, List<MaterialWithUsage>) -> Unit
) {
    var name    by remember { mutableStateOf(initialSale?.sale?.name ?: "") }
    var price   by remember { mutableStateOf(if (initialSale != null) "${initialSale.sale.salePrice}" else "") }
    var channel by remember { mutableStateOf(initialSale?.sale?.channel ?: "") }

    val initialSelectedIds = remember {
        initialSale?.materials?.map { it.material.id }?.toSet() ?: emptySet()
    }
    var selectedIds by remember { mutableStateOf(initialSelectedIds) }

    val quantities = remember {
        mutableStateMapOf<Int, String>().also { map ->
            initialSale?.materials?.forEach { usage ->
                map[usage.material.id] = formatQty(usage.usedQuantity)
            }
        }
    }

    val priceInt = price.replace(',', '.').toDoubleOrNull()?.toInt() ?: price.toIntOrNull() ?: 0

    val costSum = materials
        .filter { it.id in selectedIds }
        .sumOf { material ->
            val qty = parseQty(quantities[material.id])
            material.unitCost * qty
        }.toInt()

    val profit   = priceInt - costSum
    val isProfit = profit >= 0

    val hasQtyErrors = materials.any { material ->
        if (material.id !in selectedIds) return@any false
        val qty = parseQty(quantities[material.id])
        val available = availableStock(material, initialSale)
        qty <= 0.0 || qty > available
    }
    val isValid = name.isNotBlank() && price.isNotBlank() && channel.isNotBlank() && !hasQtyErrors

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

                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(materials) { material ->
                        val isSelected = material.id in selectedIds
                        val available  = availableStock(material, initialSale)
                        val qtyStr     = quantities[material.id] ?: ""
                        val qty        = parseQty(qtyStr)
                        val qtyError   = isSelected && (qty <= 0.0 || qty > available)

                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = isSelected,
                                        onValueChange = { checked ->
                                            selectedIds = if (checked) {
                                                if (!quantities.containsKey(material.id))
                                                    quantities[material.id] = "1"
                                                selectedIds + material.id
                                            } else {
                                                selectedIds - material.id
                                            }
                                        }
                                    )
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
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        material.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Ink
                                    )
                                    Text(
                                        "Остаток: ${formatQty(available)} ${material.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (available <= 0.0) Loss else Muted
                                    )
                                }
                            }

                            if (isSelected) {
                                OutlinedTextField(
                                    value = qtyStr,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() || it == ',' })
                                            quantities[material.id] = input
                                    },
                                    label = { Text("Кол-во (${material.unit})") },
                                    isError = qtyError,
                                    supportingText = if (qtyError) {
                                        { Text("Макс. ${formatQty(available)}", color = Loss) }
                                    } else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 40.dp)
                                        .padding(bottom = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                color = if (isProfit) ProfitBg else LossBg,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                onClick = {
                    val materialUsages = materials
                        .filter { it.id in selectedIds }
                        .mapNotNull { material ->
                            val qty = parseQty(quantities[material.id])
                            if (qty <= 0.0) null else MaterialWithUsage(material, qty)
                        }
                    onConfirm(name.trimEnd(), priceInt, channel.trimEnd(), materialUsages)
                },
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

private fun availableStock(material: MaterialEntity, initialSale: SaleWithMaterials?): Double {
    val oldUsed = initialSale?.materials?.find { it.material.id == material.id }?.usedQuantity ?: 0.0
    return material.quantity + oldUsed
}

private fun parseQty(str: String?): Double =
    str?.replace(',', '.')?.toDoubleOrNull() ?: 0.0

private fun formatQty(qty: Double): String =
    if (qty == qty.toLong().toDouble()) qty.toLong().toString() else "%.2f".format(qty)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Gold,
    unfocusedBorderColor    = Divider,
    focusedContainerColor   = GoldBg,
    unfocusedContainerColor = SurfaceAlt
)
