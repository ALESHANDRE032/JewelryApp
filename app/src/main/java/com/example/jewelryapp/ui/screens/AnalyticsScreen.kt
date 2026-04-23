package com.example.jewelryapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.SaleWithMaterials
import com.example.jewelryapp.ui.theme.*

@Composable
fun AnalyticsScreen(sales: List<SaleWithMaterials>) {
    val totalRevenue = sales.sumOf { it.sale.salePrice }
    val totalProfit  = sales.sumOf { it.sale.profit }
    val totalCost    = totalRevenue - totalProfit
    val margin       = if (totalRevenue > 0) (totalProfit * 100 / totalRevenue) else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Text("Аналитика", style = MaterialTheme.typography.displaySmall, color = Ink)

        Spacer(Modifier.height(20.dp))

        // Profit hero card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceAlt, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column {
                Text("ЧИСТАЯ ПРИБЫЛЬ", style = Eyebrow, color = Muted)
                Spacer(Modifier.height(4.dp))
                Text("$totalProfit ₽", style = DisplayXl, color = Ink)
                Spacer(Modifier.height(4.dp))
                Text(
                    "из выручки $totalRevenue ₽ · маржа $margin%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stats row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnalyticsStat("Продаж", "${sales.size}", Modifier.weight(1f))
            AnalyticsStat("Затраты", "$totalCost ₽", Modifier.weight(1f))
            AnalyticsStat(
                "Ср. чек",
                "${if (sales.isNotEmpty()) totalRevenue / sales.size else 0} ₽",
                Modifier.weight(1f)
            )
        }

        if (totalRevenue > 0) {
            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceAlt,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "Структура выручки",
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink
                    )
                    Text(
                        "Куда уходит каждый рубль",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        val costFraction   = totalCost.toFloat() / totalRevenue.toFloat()
                        val profitFraction = 1f - costFraction

                        DonutChart(
                            costFraction   = costFraction,
                            profitFraction = profitFraction,
                            centerLabel    = "ЗАТРАТЫ",
                            centerValue    = "$totalCost ₽"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LegendRow(Gold,   "Затраты",  "${(costFraction * 100).toInt()}%")
                            LegendRow(Profit, "Прибыль",  "${(profitFraction * 100).toInt()}%")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    costFraction: Float,
    profitFraction: Float,
    centerLabel: String,
    centerValue: String
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val stroke = 26.dp.toPx()
            val radius = (size.minDimension - stroke) / 2f
            val tl = Offset((size.width - radius * 2) / 2f, (size.height - radius * 2) / 2f)
            val sz = Size(radius * 2, radius * 2)

            drawArc(
                color = Gold, startAngle = -90f,
                sweepAngle = costFraction * 360f, useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Butt), topLeft = tl, size = sz
            )
            drawArc(
                color = Profit, startAngle = -90f + costFraction * 360f,
                sweepAngle = profitFraction * 360f, useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Butt), topLeft = tl, size = sz
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel, style = Eyebrow, color = Muted)
            Text(centerValue, style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
    }
}

@Composable
private fun LegendRow(color: androidx.compose.ui.graphics.Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = InkSoft, modifier = Modifier.weight(1f))
        Text(value,  style = MaterialTheme.typography.bodyLarge, color = Ink)
    }
}

@Composable
private fun AnalyticsStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = SurfaceAlt, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Muted)
            Spacer(Modifier.height(2.dp))
            Text(value, style = NumberLg, color = Ink)
        }
    }
}
