package com.example.jewelryapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Ink)
                .padding(22.dp)
        ) {
            Column {
                Text("ВЫРУЧКА ЗА МЕСЯЦ", style = Eyebrow, color = GoldSoft)
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

        if (sales.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            val bestChannelEntry = sales
                .groupBy { it.sale.channel }
                .mapValues { (_, v) -> v.sumOf { it.sale.profit } }
                .maxByOrNull { it.value }
            val bestProduct = sales.maxByOrNull { it.sale.profit }
            val worstSale   = sales.filter { it.sale.profit < 0 }.minByOrNull { it.sale.profit }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceAlt,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InsightRow(
                        label       = "ЛУЧШИЙ КАНАЛ",
                        name        = bestChannelEntry?.key ?: "—",
                        amount      = bestChannelEntry?.let { "+${it.value} ₽" } ?: "—",
                        amountColor = Profit
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Divider)
                    InsightRow(
                        label       = "ТОПОВЫЙ ТОВАР",
                        name        = bestProduct?.sale?.name ?: "—",
                        amount      = bestProduct?.let { "+${it.sale.profit} ₽" } ?: "—",
                        amountColor = Profit
                    )
                    if (worstSale != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Divider)
                        InsightRow(
                            label       = "УБЫТОЧНАЯ ПРОДАЖА",
                            name        = worstSale.sale.name,
                            amount      = "${worstSale.sale.profit} ₽",
                            amountColor = Loss
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Text("Последние продажи", style = MaterialTheme.typography.titleLarge, color = Ink)
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
private fun InsightRow(label: String, name: String, amount: String, amountColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = Eyebrow, color = Muted)
            Spacer(Modifier.height(2.dp))
            Text(name, style = MaterialTheme.typography.titleMedium, color = Ink)
        }
        Text(amount, style = NumberLg, color = amountColor)
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
