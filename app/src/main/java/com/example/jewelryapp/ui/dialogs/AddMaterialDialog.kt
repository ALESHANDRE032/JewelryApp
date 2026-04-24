package com.example.jewelryapp.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.MaterialEntity
import com.example.jewelryapp.ui.theme.*

private val UNITS = listOf("шт", "гр", "м", "см")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaterialBottomSheet(
    initialMaterial: MaterialEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Double, String) -> Unit
) {
    var name     by remember { mutableStateOf(initialMaterial?.name ?: "") }
    var cost     by remember { mutableStateOf(if (initialMaterial != null) "${initialMaterial.cost}" else "") }
    var quantity by remember { mutableStateOf(if (initialMaterial != null) formatQty(initialMaterial.quantity) else "") }
    var unit     by remember { mutableStateOf(initialMaterial?.unit ?: "шт") }
    var unitMenuExpanded by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() && cost.isNotBlank() && quantity.isNotBlank()

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
                if (initialMaterial != null) "Редактировать материал" else "В запасы мастерской",
                style = Eyebrow,
                color = Muted
            )
            Text(
                if (initialMaterial != null) "Изменить материал" else "Новый материал",
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
                value = cost,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == ',' }) cost = input
                },
                label = { Text("Цена закупки, ₽") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == ',' }) quantity = input
                    },
                    label = { Text("Количество") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                ExposedDropdownMenuBox(
                    expanded = unitMenuExpanded,
                    onExpandedChange = { unitMenuExpanded = it },
                    modifier = Modifier.width(100.dp)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ед.") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors(),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = unitMenuExpanded,
                        onDismissRequest = { unitMenuExpanded = false },
                        containerColor = Surface
                    ) {
                        UNITS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, style = MaterialTheme.typography.bodyLarge, color = Ink) },
                                onClick = {
                                    unit = option
                                    unitMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
                ) {
                    Text("Отмена", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = {
                        val costInt = cost.replace(',', '.').toDoubleOrNull()?.toInt()
                            ?: cost.toIntOrNull() ?: 0
                        val qtyDouble = quantity.replace(',', '.').toDoubleOrNull()
                            ?: quantity.toDoubleOrNull() ?: 1.0
                        onConfirm(name.trimEnd(), costInt, qtyDouble, unit)
                    },
                    enabled = isValid,
                    modifier = Modifier.weight(2f).height(52.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = Ink,
                        contentColor           = Surface,
                        disabledContainerColor = SurfaceDeep,
                        disabledContentColor   = Muted
                    )
                ) {
                    Text(
                        if (initialMaterial != null) "Сохранить" else "Сохранить материал",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Gold,
    unfocusedBorderColor    = Divider,
    focusedContainerColor   = GoldBg,
    unfocusedContainerColor = SurfaceAlt
)

private fun formatQty(qty: Double): String =
    if (qty == qty.toLong().toDouble()) qty.toLong().toString() else "%.2f".format(qty)
