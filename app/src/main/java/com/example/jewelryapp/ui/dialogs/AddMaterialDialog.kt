package com.example.jewelryapp.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaterialBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() && cost.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor  = Surface,
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
                "В запасы мастерской",
                style = com.example.jewelryapp.ui.theme.Eyebrow,
                color = Muted
            )
            Text(
                "Новый материал",
                style = MaterialTheme.typography.displaySmall,
                color = Ink
            )

            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Gold,
                    unfocusedBorderColor = Divider,
                    focusedContainerColor   = GoldBg,
                    unfocusedContainerColor = SurfaceAlt
                )
            )

            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Цена закупки, ₽") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Gold,
                    unfocusedBorderColor = Divider,
                    focusedContainerColor   = GoldBg,
                    unfocusedContainerColor = SurfaceAlt
                )
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
                    onClick = { onConfirm(name, cost.toIntOrNull() ?: 0) },
                    enabled = isValid,
                    modifier = Modifier.weight(2f).height(52.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ink,
                        contentColor   = Surface,
                        disabledContainerColor = SurfaceDeep,
                        disabledContentColor   = Muted
                    )
                ) {
                    Text("Сохранить материал", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
