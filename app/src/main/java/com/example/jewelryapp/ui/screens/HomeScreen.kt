package com.example.jewelryapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.MaterialEntity
import com.example.jewelryapp.data.SaleWithMaterials
import com.example.jewelryapp.ui.components.SaleCard
import com.example.jewelryapp.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    sales: List<SaleWithMaterials>,
    materials: List<MaterialEntity>,
    onAddSale: () -> Unit,
    onAddMaterial: () -> Unit
) {
    val totalRevenue = sales.sumOf { it.sale.salePrice }
    val totalProfit  = sales.sumOf { it.sale.profit }
    val totalCost    = totalRevenue - totalProfit
    val margin       = if (totalRevenue > 0) (totalProfit * 100 / totalRevenue) else 0
    val month        = LocalDate.now()
        .month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        .replaceFirstChar { it.uppercase() }
    val heroSubtitle = if (totalProfit >= 0) "$month идёт хорошо." else "$month был непростым."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text("Мастерская", style = MaterialTheme.typography.bodyLarge, color = Muted)
        Spacer(Modifier.height(4.dp))
        Text(heroSubtitle, style = DisplayMd, color = Ink)

        Spacer(Modifier.height(20.dp))

        // Hero card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Ink)
                .padding(22.dp)
        ) {
            Column {
                Text(
                    "ВЫРУЧКА ЗА МЕСЯЦ",
                    style = Eyebrow,
                    color = GoldSoft
                )
                Spacer(Modifier.height(4.dp))
                Text("$totalRevenue ₽", style = DisplayLg, color = Surface)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0x40D9BF8A), thickness = 1.dp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    HeroStat("ПРИБЫЛЬ", "$totalProfit ₽")
                    HeroStat("МАРЖА", "$margin%")
                    HeroStat("ПРОДАЖ", "${sales.size}")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Action buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onAddSale,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Surface)
            ) {
                Text("+ Продажа", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onAddMaterial,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
            ) {
                Text("Материал", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(28.dp))

        if (sales.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Последние продажи", style = MaterialTheme.typography.titleLarge, color = Ink)
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sales.take(3).forEach { sale ->
                    SaleCard(sale = sale, onDelete = null)
                }
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    Column {
        Text(label, style = Eyebrow, color = GoldSoft)
        Spacer(Modifier.height(2.dp))
        Text(value, style = NumberLg, color = Surface)
    }
}
