package com.example.jewelryapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.ExpenseType
import com.example.jewelryapp.data.SaleWithMaterials
import com.example.jewelryapp.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.Canvas

private enum class Period { WEEK, MONTH, QUARTER, YEAR }

private data class ChartPoint(val label: String, val value: Float, val showLabel: Boolean)

private val segmentColors = listOf(
    Ink,
    Gold,
    GoldSoft,
    Color(0xFFCBBB99),
    Color(0xFFDED0B8),
    Color(0xFFEEE6D8)
)

@Composable
fun AnalyticsScreen(sales: List<SaleWithMaterials>) {
    var period by remember { mutableStateOf(Period.MONTH) }
    val today  = remember { LocalDate.now() }

    val (periodStart, periodLen) = remember(period, today) {
        when (period) {
            Period.WEEK    -> today.minusDays(6) to 7L
            Period.MONTH   -> today.withDayOfMonth(1) to today.dayOfMonth.toLong()
            Period.QUARTER -> {
                val qStart = today.withMonth(((today.monthValue - 1) / 3) * 3 + 1).withDayOfMonth(1)
                qStart to (today.toEpochDay() - qStart.toEpochDay() + 1)
            }
            Period.YEAR    -> today.withDayOfYear(1) to today.dayOfYear.toLong()
        }
    }

    val startEpoch = periodStart.toEpochDay()
    val endEpoch   = today.toEpochDay()

    val filteredSales = remember(sales, startEpoch, endEpoch) {
        sales.filter { it.sale.saleDate in startEpoch..endEpoch }
    }
    val legacyCount = remember(sales) { sales.count { it.sale.saleDate == 0L } }

    val prevStart   = startEpoch - periodLen
    val prevSales   = remember(sales, prevStart, startEpoch) {
        sales.filter { it.sale.saleDate in prevStart until startEpoch }
    }

    val totalProfit  = filteredSales.sumOf { it.sale.profit }
    val totalRevenue = filteredSales.sumOf { it.sale.salePrice }
    val totalCost    = totalRevenue - totalProfit
    val margin       = if (totalRevenue > 0) (totalProfit * 100 / totalRevenue) else 0

    val prevProfit = prevSales.sumOf { it.sale.profit }
    val trendPct: Int? = if (prevProfit != 0) ((totalProfit - prevProfit) * 100 / prevProfit) else null

    val chartPoints = remember(filteredSales, period, periodStart, today) {
        buildChartPoints(filteredSales, period, periodStart, today)
    }

    // Cost breakdown using stored fields only
    val materialsCost = filteredSales.sumOf { sale ->
        (sale.sale.salePrice - sale.sale.profit - sale.sale.extraExpensesCost).coerceAtLeast(0)
    }
    val expensesByType: Map<String, Int> = filteredSales
        .flatMap { it.expenses }
        .groupBy { it.expenseType }
        .mapValues { (_, v) -> v.sumOf { it.amount } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Text("Аналитика", style = MaterialTheme.typography.displaySmall, color = Ink)

        Spacer(Modifier.height(16.dp))

        // Period chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Period.WEEK    to "Неделя",
                Period.MONTH   to "Месяц",
                Period.QUARTER to "Квартал",
                Period.YEAR    to "Год"
            ).forEach { (p, label) ->
                PeriodChip(label = label, selected = period == p, onClick = { period = p })
            }
        }

        Spacer(Modifier.height(16.dp))

        // Hero card with chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceAlt, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ЧИСТАЯ ПРИБЫЛЬ", style = Eyebrow, color = Muted)
                    TrendBadge(trendPct)
                }
                Spacer(Modifier.height(4.dp))
                Text("$totalProfit ₽", style = DisplayXl, color = Ink)
                Spacer(Modifier.height(4.dp))
                Text(
                    "из выручки $totalRevenue ₽ · маржа $margin%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted
                )

                if (chartPoints.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    ProfitLineChart(
                        points = chartPoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Stats row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalyticsStat("Продаж",  "${filteredSales.size}", Modifier.weight(1f))
            AnalyticsStat("Затраты", "$totalCost ₽",          Modifier.weight(1f))
            AnalyticsStat(
                "Ср. чек",
                "${if (filteredSales.isNotEmpty()) totalRevenue / filteredSales.size else 0} ₽",
                Modifier.weight(1f)
            )
        }

        if (totalCost > 0) {
            Spacer(Modifier.height(16.dp))

            // Cost breakdown card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceAlt,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Структура затрат", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text("Куда уходит каждый рубль", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    Spacer(Modifier.height(16.dp))

                    val segments = buildCostSegments(materialsCost, expensesByType)

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        MultiSegmentDonut(
                            segments   = segments,
                            totalLabel = "ЗАТРАТЫ",
                            totalValue = "$totalCost ₽"
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            segments.forEach { (color, label, fraction) ->
                                LegendRow(color, label, "${(fraction * 100).toInt()}%")
                            }
                        }
                    }
                }
            }
        }

        if (legacyCount > 0) {
            Spacer(Modifier.height(10.dp))
            Text(
                "$legacyCount продаж без даты не включены в статистику периода",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSoft
            )
        }
    }
}

// ── Period chip ────────────────────────────────────────────────────────────────

@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick   = onClick,
        color     = if (selected) Ink else SurfaceAlt,
        shape     = RoundedCornerShape(100.dp),
        tonalElevation = 0.dp
    ) {
        Text(
            text     = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style    = MaterialTheme.typography.bodyMedium,
            color    = if (selected) Surface else Muted
        )
    }
}

// ── Trend badge ────────────────────────────────────────────────────────────────

@Composable
private fun TrendBadge(pct: Int?) {
    if (pct == null) return
    val positive = pct >= 0
    Surface(
        color = if (positive) ProfitBg else LossBg,
        shape = RoundedCornerShape(100.dp)
    ) {
        Text(
            text     = "${if (positive) "↑ +" else "↓ "}$pct%",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style    = MaterialTheme.typography.bodyMedium,
            color    = if (positive) Profit else Loss
        )
    }
}

// ── Line chart ─────────────────────────────────────────────────────────────────

@Composable
private fun ProfitLineChart(points: List<ChartPoint>, modifier: Modifier = Modifier) {
    if (points.size < 2) return

    val maxVal = points.maxOf { it.value }
    val minVal = points.minOf { it.value }.coerceAtMost(0f)
    val range  = (maxVal - minVal).coerceAtLeast(1f)

    val labelPaint = remember {
        android.graphics.Paint().apply {
            color       = android.graphics.Color.argb(140, 138, 130, 122)
            textSize    = 30f
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier) {
        val labelH = 28f
        val chartH = size.height - labelH
        val chartW = size.width
        val n      = points.size
        val stepX  = chartW / (n - 1).toFloat()

        fun xAt(i: Int) = i * stepX
        fun yAt(v: Float) = chartH - ((v - minVal) / range) * chartH * 0.88f - chartH * 0.06f

        // Area fill
        val fillPath = Path().apply {
            moveTo(xAt(0), yAt(points[0].value))
            for (i in 1 until n) lineTo(xAt(i), yAt(points[i].value))
            lineTo(xAt(n - 1), chartH)
            lineTo(xAt(0), chartH)
            close()
        }
        drawPath(fillPath, Gold.copy(alpha = 0.13f))

        // Line
        val linePath = Path().apply {
            moveTo(xAt(0), yAt(points[0].value))
            for (i in 1 until n) lineTo(xAt(i), yAt(points[i].value))
        }
        drawPath(
            linePath,
            Gold,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // End dot
        drawCircle(Gold, 4.dp.toPx(), Offset(xAt(n - 1), yAt(points.last().value)))

        // X-axis labels
        drawIntoCanvas { canvas ->
            points.forEachIndexed { i, pt ->
                if (pt.showLabel && pt.label.isNotEmpty()) {
                    val x = xAt(i)
                    val tw = labelPaint.measureText(pt.label)
                    val tx = (x - tw / 2f).coerceIn(0f, chartW - tw)
                    canvas.nativeCanvas.drawText(pt.label, tx, size.height - 2f, labelPaint)
                }
            }
        }
    }
}

// ── Chart data builder ─────────────────────────────────────────────────────────

private fun buildChartPoints(
    sales: List<SaleWithMaterials>,
    period: Period,
    periodStart: LocalDate,
    today: LocalDate
): List<ChartPoint> {
    return when (period) {
        Period.WEEK -> (0L..6L).map { offset ->
            val day   = periodStart.plusDays(offset)
            val epoch = day.toEpochDay()
            val profit = sales.filter { it.sale.saleDate == epoch }.sumOf { it.sale.profit }.toFloat()
            val label  = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ru"))
                .replaceFirstChar { it.uppercase() }
            ChartPoint(label, profit, showLabel = true)
        }

        Period.MONTH -> {
            val daysInMonth = periodStart.lengthOfMonth()
            (1..daysInMonth).map { d ->
                val date  = periodStart.withDayOfMonth(d)
                val epoch = date.toEpochDay()
                val profit = sales.filter { it.sale.saleDate == epoch }.sumOf { it.sale.profit }.toFloat()
                val show  = d == 1 || d % 6 == 1 || d == daysInMonth
                val label = if (show) "$d ${date.month.getDisplayName(TextStyle.SHORT, Locale("ru"))}" else ""
                ChartPoint(label, profit, showLabel = show)
            }
        }

        Period.QUARTER -> {
            val firstMonth = periodStart.withDayOfMonth(1)
            (0..2).map { i ->
                val month      = firstMonth.plusMonths(i.toLong())
                val monthStart = month.toEpochDay()
                val monthEnd   = month.plusMonths(1).minusDays(1).toEpochDay()
                val profit     = sales.filter { it.sale.saleDate in monthStart..monthEnd }
                    .sumOf { it.sale.profit }.toFloat()
                val label = month.month.getDisplayName(TextStyle.SHORT, Locale("ru"))
                    .replaceFirstChar { it.uppercase() }
                ChartPoint(label, profit, showLabel = true)
            }
        }

        Period.YEAR -> (1..12).map { m ->
            val month      = LocalDate.of(today.year, m, 1)
            val monthStart = month.toEpochDay()
            val monthEnd   = month.plusMonths(1).minusDays(1).toEpochDay()
            val profit     = sales.filter { it.sale.saleDate in monthStart..monthEnd }
                .sumOf { it.sale.profit }.toFloat()
            val label = month.month.getDisplayName(TextStyle.SHORT, Locale("ru"))
                .replaceFirstChar { it.uppercase() }
            ChartPoint(label, profit, showLabel = true)
        }
    }
}

// ── Cost segments ──────────────────────────────────────────────────────────────

private data class CostSegment(val color: Color, val label: String, val fraction: Float)

private fun buildCostSegments(
    materialsCost: Int,
    expensesByType: Map<String, Int>
): List<CostSegment> {
    val items = mutableListOf<Pair<String, Int>>()
    if (materialsCost > 0) items.add("Материалы" to materialsCost)
    ExpenseType.ALL.forEach { option ->
        val amt = expensesByType[option.key] ?: 0
        if (amt > 0) items.add(option.label to amt)
    }
    val total = items.sumOf { it.second }.toFloat().coerceAtLeast(1f)
    return items.mapIndexed { i, (label, amount) ->
        CostSegment(
            color    = segmentColors.getOrElse(i) { Muted },
            label    = label,
            fraction = amount / total
        )
    }
}

// ── Multi-segment donut ────────────────────────────────────────────────────────

@Composable
private fun MultiSegmentDonut(
    segments: List<CostSegment>,
    totalLabel: String,
    totalValue: String
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val stroke = 26.dp.toPx()
            val radius = (size.minDimension - stroke) / 2f
            val tl     = Offset((size.width - radius * 2) / 2f, (size.height - radius * 2) / 2f)
            val sz     = Size(radius * 2, radius * 2)
            var start  = -90f
            segments.forEach { seg ->
                val sweep = seg.fraction * 360f
                drawArc(
                    color      = seg.color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter  = false,
                    style      = Stroke(stroke, cap = StrokeCap.Butt),
                    topLeft    = tl,
                    size       = sz
                )
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(totalLabel, style = Eyebrow, color = Muted)
            Text(totalValue, style = MaterialTheme.typography.bodyLarge, color = Ink)
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

@Composable
private fun LegendRow(color: Color, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = InkSoft, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Ink)
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
