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
import com.example.jewelryapp.data.ProductWithMaterials
import com.example.jewelryapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductBottomSheet(
    materials: List<MaterialEntity>,
    initialProduct: ProductWithMaterials? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, expectedSalePrice: Int, materials: List<MaterialWithUsage>) -> Unit
) {
    var name  by remember { mutableStateOf(initialProduct?.product?.name ?: "") }
    var price by remember { mutableStateOf(if (initialProduct != null) "${initialProduct.product.expectedSalePrice}" else "") }

    val initialSelectedIds = remember {
        initialProduct?.materials?.map { it.material.id }?.toSet() ?: emptySet()
    }
    var selectedIds by remember { mutableStateOf(initialSelectedIds) }

    val quantities = remember {
        mutableStateMapOf<Int, String>().also { map ->
            initialProduct?.materials?.forEach { usage ->
                map[usage.material.id] = formatProductQty(usage.usedQuantity)
            }
        }
    }

    val priceInt = price.replace(',', '.').toDoubleOrNull()?.toInt() ?: price.toIntOrNull() ?: 0

    val expectedCost = materials
        .filter { it.id in selectedIds }
        .sumOf { material ->
            val qty = parseProductQty(quantities[material.id])
            material.unitCost * qty
        }.toInt()

    val expectedProfit = priceInt - expectedCost
    val isProfit = expectedProfit >= 0

    val hasQtyErrors = materials.any { material ->
        if (material.id !in selectedIds) return@any false
        val qty = parseProductQty(quantities[material.id])
        val available = availableProductStock(material, initialProduct)
        qty <= 0.0 || qty > available
    }
    val isValid = name.isNotBlank() && price.isNotBlank() && !hasQtyErrors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
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
                if (initialProduct != null) "Редактировать товар" else "Новый товар",
                style = MaterialTheme.typography.displaySmall,
                color = Ink
            )

            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.replaceFirstChar { c -> c.uppercase() } },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = productFieldColors()
            )
            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == ',' }) price = input
                },
                label = { Text("Ожидаемая цена продажи, ₽") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = productFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (materials.isNotEmpty()) {
                Text("Материалы:", style = MaterialTheme.typography.titleMedium, color = Ink)

                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(materials) { material ->
                        val isSelected = material.id in selectedIds
                        val available  = availableProductStock(material, initialProduct)
                        val qtyStr     = quantities[material.id] ?: ""
                        val qty        = parseProductQty(qtyStr)
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
                                        "Остаток: ${formatProductQty(available)} ${material.unit}",
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
                                        { Text("Макс. ${formatProductQty(available)}", color = Loss) }
                                    } else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 40.dp)
                                        .padding(bottom = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = productFieldColors(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // Cost / profit summary
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
                        Text("$expectedCost ₽", style = NumberLg, color = Ink)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ПРИБЫЛЬ", style = Eyebrow, color = Muted)
                        Text(
                            "$expectedProfit ₽",
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
                            val qty = parseProductQty(quantities[material.id])
                            if (qty <= 0.0) null else MaterialWithUsage(material, qty)
                        }
                    onConfirm(name.trimEnd(), priceInt, materialUsages)
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = Ink,
                    contentColor           = Surface,
                    disabledContainerColor = SurfaceDeep,
                    disabledContentColor   = Muted
                )
            ) {
                Text(
                    if (initialProduct != null) "Сохранить" else "Создать товар",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun availableProductStock(material: MaterialEntity, initialProduct: ProductWithMaterials?): Double {
    val oldUsed = initialProduct?.materials?.find { it.material.id == material.id }?.usedQuantity ?: 0.0
    return material.quantity + oldUsed
}

private fun parseProductQty(str: String?): Double =
    str?.replace(',', '.')?.toDoubleOrNull() ?: 0.0

private fun formatProductQty(qty: Double): String =
    if (qty == qty.toLong().toDouble()) qty.toLong().toString() else "%.2f".format(qty)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun productFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Gold,
    unfocusedBorderColor    = Divider,
    focusedContainerColor   = GoldBg,
    unfocusedContainerColor = SurfaceAlt
)
