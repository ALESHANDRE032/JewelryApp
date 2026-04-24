package com.example.jewelryapp.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.jewelryapp.data.ProductEntity
import com.example.jewelryapp.data.SaleExpenseEntity
import com.example.jewelryapp.data.SaleWithMaterials
import com.example.jewelryapp.ui.theme.*

private data class ExpenseOption(val key: String, val label: String)

private val EXPENSE_OPTIONS = listOf(
    ExpenseOption("RENT",              "Аренда"),
    ExpenseOption("DELIVERY_TO_STORE", "Доставка в магазин"),
    ExpenseOption("COURIER",           "Курьер / доставка клиенту"),
    ExpenseOption("STORE_COMMISSION",  "Комиссия магазина")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleBottomSheet(
    products: List<ProductEntity>,
    initialSale: SaleWithMaterials? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Int, channel: String, productId: Int?, expenses: List<SaleExpenseEntity>) -> Unit
) {
    var name    by remember { mutableStateOf(initialSale?.sale?.name ?: "") }
    var price   by remember { mutableStateOf(if (initialSale != null) "${initialSale.sale.salePrice}" else "") }
    var channel by remember { mutableStateOf(initialSale?.sale?.channel ?: "") }

    var selectedProductId   by remember { mutableStateOf<Int?>(initialSale?.sale?.productId) }
    var productMenuExpanded by remember { mutableStateOf(false) }

    val selectedExpenseTypes = remember {
        mutableStateOf(initialSale?.expenses?.map { it.expenseType }?.toSet() ?: emptySet<String>())
    }
    val expenseAmounts = remember {
        mutableStateMapOf<String, String>().also { map ->
            initialSale?.expenses?.forEach { e -> map[e.expenseType] = "${e.amount}" }
        }
    }

    // Products available for selection: qty > 0, OR the one already selected in edit mode
    val availableProducts = remember(products, initialSale) {
        val editProductId = initialSale?.sale?.productId
        products.filter { it.quantity > 0 || it.id == editProductId }
    }

    val selectedProduct = availableProducts.find { it.id == selectedProductId }
    val productCost     = selectedProduct?.expectedCost ?: 0
    val priceInt        = price.replace(',', '.').toDoubleOrNull()?.toInt() ?: price.toIntOrNull() ?: 0

    val expenseTotal = selectedExpenseTypes.value.sumOf { type ->
        expenseAmounts[type]?.replace(',', '.')?.toDoubleOrNull()?.toInt() ?: 0
    }
    val profit   = priceInt - productCost - expenseTotal
    val isProfit = profit >= 0

    val isValid = name.isNotBlank() && price.isNotBlank() && channel.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (initialSale != null) "Редактировать продажу" else "Новая продажа",
                style = MaterialTheme.typography.displaySmall,
                color = Ink
            )

            Spacer(Modifier.height(4.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.replaceFirstChar { c -> c.uppercase() } },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = saleFieldColors()
            )

            // Product selector
            ExposedDropdownMenuBox(
                expanded = productMenuExpanded,
                onExpandedChange = { productMenuExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedProduct?.name ?: "Без товара",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Товар") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = productMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = saleFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = productMenuExpanded,
                    onDismissRequest = { productMenuExpanded = false },
                    containerColor = Surface
                ) {
                    DropdownMenuItem(
                        text = { Text("Без товара", style = MaterialTheme.typography.bodyLarge, color = Muted) },
                        onClick = {
                            selectedProductId = null
                            productMenuExpanded = false
                        }
                    )
                    availableProducts.forEach { product ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(product.name, style = MaterialTheme.typography.bodyLarge, color = Ink)
                                    Text(
                                        "Остаток: ${product.quantity} · ${product.expectedSalePrice} ₽",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Muted
                                    )
                                }
                            },
                            onClick = {
                                selectedProductId = product.id
                                if (price.isEmpty()) price = "${product.expectedSalePrice}"
                                productMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Price field
            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == ',' }) price = input
                },
                label = { Text("Цена продажи, ₽") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = saleFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Channel field
            OutlinedTextField(
                value = channel,
                onValueChange = { channel = it.replaceFirstChar { c -> c.uppercase() } },
                label = { Text("Канал продажи") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = saleFieldColors()
            )

            // Expenses section
            Text("Дополнительные расходы:", style = MaterialTheme.typography.titleMedium, color = Ink)

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                EXPENSE_OPTIONS.forEach { option ->
                    val isChecked = option.key in selectedExpenseTypes.value
                    val amountStr = expenseAmounts[option.key] ?: ""
                    val amountErr = isChecked && (amountStr.isBlank() ||
                            (amountStr.replace(',', '.').toDoubleOrNull() ?: 0.0) < 0)

                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedExpenseTypes.value = if (checked) {
                                        if (!expenseAmounts.containsKey(option.key))
                                            expenseAmounts[option.key] = ""
                                        selectedExpenseTypes.value + option.key
                                    } else {
                                        selectedExpenseTypes.value - option.key
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor   = Gold,
                                    uncheckedColor = Divider
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Ink
                            )
                        }
                        if (isChecked) {
                            OutlinedTextField(
                                value = amountStr,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() || it == ',' })
                                        expenseAmounts[option.key] = input
                                },
                                label = { Text("Сумма, ₽") },
                                isError = amountErr,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 40.dp)
                                    .padding(bottom = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = saleFieldColors(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Profit summary
            Surface(
                color = if (isProfit) ProfitBg else LossBg,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("СЕБЕСТОИМОСТЬ", style = Eyebrow, color = Muted)
                            Text("$productCost ₽", style = NumberLg, color = Ink)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("РАСХОДЫ", style = Eyebrow, color = Muted)
                            Text("$expenseTotal ₽", style = NumberLg, color = Ink)
                        }
                    }
                    HorizontalDivider(color = androidx.compose.ui.graphics.Color(0x20000000), thickness = 1.dp)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ПРИБЫЛЬ", style = Eyebrow, color = Muted)
                        Text(
                            "$profit ₽",
                            style = NumberLg,
                            color = if (isProfit) Profit else Loss
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val expenseEntities = EXPENSE_OPTIONS
                        .filter { it.key in selectedExpenseTypes.value }
                        .mapNotNull { option ->
                            val amount = expenseAmounts[option.key]
                                ?.replace(',', '.')?.toDoubleOrNull()?.toInt() ?: return@mapNotNull null
                            if (amount <= 0) null
                            else SaleExpenseEntity(saleId = 0, expenseType = option.key, amount = amount)
                        }
                    onConfirm(name.trimEnd(), priceInt, channel.trimEnd(), selectedProductId, expenseEntities)
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = Ink,
                    contentColor           = Surface,
                    disabledContainerColor = SurfaceDeep,
                    disabledContentColor   = Muted
                )
            ) {
                Text(
                    if (initialSale != null) "Сохранить" else "Добавить продажу",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun saleFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Gold,
    unfocusedBorderColor    = Divider,
    focusedContainerColor   = GoldBg,
    unfocusedContainerColor = SurfaceAlt
)
