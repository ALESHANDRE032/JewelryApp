package com.example.jewelryapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.SaleWithMaterials
import com.example.jewelryapp.ui.theme.*

@Composable
fun SaleCard(sale: SaleWithMaterials, onDelete: (() -> Unit)?) {
    val margin = if (sale.sale.salePrice > 0)
        (sale.sale.profit * 100 / sale.sale.salePrice) else 0
    val isProfit = sale.sale.profit >= 0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceAlt,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(GoldBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sale.sale.name.first().uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldInk
                )
            }

            // Name + meta
            Column(Modifier.weight(1f)) {
                Text(sale.sale.name, style = MaterialTheme.typography.titleMedium, color = Ink)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${sale.sale.salePrice} ₽ · ${sale.sale.channel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
            }

            // Trailing: profit chip + optional delete
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Profit chip
                Box(
                    modifier = Modifier
                        .background(
                            if (isProfit) ProfitBg else LossBg,
                            RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${if (isProfit) "↑" else "↓"} $margin%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isProfit) Profit else Loss
                    )
                }

                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            "Удалить",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Loss
                        )
                    }
                }
            }
        }
    }
}
