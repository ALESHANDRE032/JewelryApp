package com.example.jewelryapp.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
                onClick = { onConfirm(name, cost.toIntOrNull() ?: 0) },
                enabled = isValid
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Отмена") }
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
