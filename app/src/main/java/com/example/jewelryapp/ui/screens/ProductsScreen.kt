package com.example.jewelryapp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.ProductWithMaterials
import com.example.jewelryapp.ui.theme.*

@Composable
fun ProductsScreen(
    products: List<ProductWithMaterials>,
    onAddProduct: () -> Unit,
    onEditProduct: (ProductWithMaterials) -> Unit,
    onDeleteProduct: (ProductWithMaterials) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProduct,
                containerColor = Ink,
                contentColor = Surface,
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("+ Товар", style = MaterialTheme.typography.labelLarge)
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
                    Text(
                        "${products.size} позиций",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted
                    )
                    Text("Товары", style = MaterialTheme.typography.displaySmall, color = Ink)
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (products.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Нет товаров",
                                style = MaterialTheme.typography.titleMedium,
                                color = Muted
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Нажми «+ Товар», чтобы добавить первый",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSoft
                            )
                        }
                    }
                }
            }

            items(products) { product ->
                ProductCard(
                    product  = product,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onEdit   = { onEditProduct(product) },
                    onDelete = { onDeleteProduct(product) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductWithMaterials,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val expectedProfit = product.product.expectedSalePrice - product.product.expectedCost
    val isProfit = expectedProfit >= 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceAlt,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(GoldBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    product.product.name.first().uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldInk
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    product.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${product.product.expectedSalePrice} ₽ · Себ: ${product.product.expectedCost} ₽",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${if (isProfit) "+" else ""}$expectedProfit ₽",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isProfit) Profit else Loss
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Остаток: ${product.product.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.product.quantity <= 0) Loss else Muted
                )
                Spacer(Modifier.height(4.dp))
                Row {
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
    }
}
