package com.example.jewelryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.MaterialEntity
import com.example.jewelryapp.ui.theme.*

@Composable
fun MaterialsScreen(
    materials: List<MaterialEntity>,
    onAddMaterial: () -> Unit,
    onEditMaterial: (MaterialEntity) -> Unit,
    onDeleteMaterial: (MaterialEntity) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMaterial,
                containerColor = Ink,
                contentColor = Surface,
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("+ Материал", style = MaterialTheme.typography.labelLarge)
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
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 8.dp)
                ) {
                    Text("${materials.size} позиций", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    Text("Материалы", style = MaterialTheme.typography.displaySmall, color = Ink)
                    Spacer(Modifier.height(8.dp))
                }
            }

            items(materials) { material ->
                MaterialRow(
                    material = material,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onEdit   = { onEditMaterial(material) },
                    onDelete = { onDeleteMaterial(material) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MaterialRow(
    material: MaterialEntity,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceAlt,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = material.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Остаток: ${formatQty(material.quantity)} ${material.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (material.quantity <= 0.0) Loss else Muted
                )
            }
            Text(
                text = "${material.cost} ₽",
                style = NumberLg,
                color = Gold
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Изменить",
                    tint = Muted,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint = Loss,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatQty(qty: Double): String =
    if (qty == qty.toLong().toDouble()) qty.toLong().toString() else "%.2f".format(qty)
