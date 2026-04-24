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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaterialBottomSheet(
    initialMaterial: MaterialEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialMaterial?.name ?: "") }
    var cost by remember { mutableStateOf(if (initialMaterial != null) "${initialMaterial.cost}" else "") }

    val isValid = name.isNotBlank() && cost.isNotBlank()

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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = Gold,
                    unfocusedBorderColor    = Divider,
                    focusedContainerColor   = GoldBg,
                    unfocusedContainerColor = SurfaceAlt
                )
            )

            OutlinedTextField(
                value = cost,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == ',' }) cost = input
                },
                label = { Text("Цена закупки, ₽") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = Gold,
                    unfocusedBorderColor    = Divider,
                    focusedContainerColor   = GoldBg,
                    unfocusedContainerColor = SurfaceAlt
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

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
                        onConfirm(name.trimEnd(), costInt)
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
