package com.example.jewelryapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.jewelryapp.JewelryViewModel
import com.example.jewelryapp.data.MaterialEntity
import com.example.jewelryapp.data.ProductWithMaterials
import com.example.jewelryapp.data.SaleWithMaterials
import com.example.jewelryapp.ui.dialogs.AddMaterialBottomSheet
import com.example.jewelryapp.ui.dialogs.AddProductBottomSheet
import com.example.jewelryapp.ui.dialogs.AddSaleBottomSheet
import com.example.jewelryapp.ui.theme.GoldBg
import com.example.jewelryapp.ui.theme.GoldInk
import com.example.jewelryapp.ui.theme.Ink
import com.example.jewelryapp.ui.theme.Surface

private enum class AppTab { Home, Sales, Products, Materials, Analytics }

@Composable
fun JewelryAppScreen(viewModel: JewelryViewModel) {
    var selectedTab       by remember { mutableStateOf(AppTab.Home) }
    var showSaleSheet     by remember { mutableStateOf(false) }
    var showMaterialSheet by remember { mutableStateOf(false) }
    var showProductSheet  by remember { mutableStateOf(false) }
    var editingSale       by remember { mutableStateOf<SaleWithMaterials?>(null) }
    var editingMaterial   by remember { mutableStateOf<MaterialEntity?>(null) }
    var editingProduct    by remember { mutableStateOf<ProductWithMaterials?>(null) }

    val sales     by viewModel.sales.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val products  by viewModel.products.collectAsState()
    val error     by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = Surface, contentColor = Ink) {
                listOf(
                    Triple(AppTab.Home,      "Главная",   Icons.Filled.Home),
                    Triple(AppTab.Sales,     "Продажи",   Icons.Filled.ShoppingCart),
                    Triple(AppTab.Products,  "Товары",    Icons.Filled.Category),
                    Triple(AppTab.Materials, "Материалы", Icons.Filled.Star),
                    Triple(AppTab.Analytics, "Аналитика", Icons.Filled.List)
                ).forEach { (tab, label, icon) ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick  = { selectedTab = tab },
                        icon  = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = GoldInk,
                            selectedTextColor   = GoldInk,
                            indicatorColor      = GoldBg,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (selectedTab) {
                AppTab.Home -> HomeScreen(
                    sales         = sales,
                    materials     = materials,
                    onAddSale     = { showSaleSheet = true },
                    onAddMaterial = { showMaterialSheet = true }
                )
                AppTab.Sales -> SalesScreen(
                    sales        = sales,
                    onDeleteSale = { viewModel.deleteSale(it) },
                    onEditSale   = { editingSale = it },
                    onAddSale    = { showSaleSheet = true }
                )
                AppTab.Products -> ProductsScreen(
                    products        = products,
                    onAddProduct    = { showProductSheet = true },
                    onEditProduct   = { editingProduct = it },
                    onDeleteProduct = { viewModel.deleteProduct(it) }
                )
                AppTab.Materials -> MaterialsScreen(
                    materials        = materials,
                    onAddMaterial    = { showMaterialSheet = true },
                    onEditMaterial   = { editingMaterial = it },
                    onDeleteMaterial = { viewModel.deleteMaterial(it) }
                )
                AppTab.Analytics -> AnalyticsScreen(sales = sales)
            }
        }
    }

    // ── Sale sheets ────────────────────────────────────────────────────────────

    if (showSaleSheet) {
        AddSaleBottomSheet(
            products  = products.map { it.product },
            onDismiss = { showSaleSheet = false },
            onConfirm = { name, price, channel, productId, expenses ->
                viewModel.addSale(name, price, channel, productId, expenses)
                showSaleSheet = false
            }
        )
    }

    editingSale?.let { sale ->
        AddSaleBottomSheet(
            products     = products.map { it.product },
            initialSale  = sale,
            onDismiss    = { editingSale = null },
            onConfirm    = { name, price, channel, productId, expenses ->
                viewModel.updateSale(sale.sale.id, name, price, channel, sale, productId, expenses)
                editingSale = null
            }
        )
    }

    // ── Product sheets ─────────────────────────────────────────────────────────

    if (showProductSheet) {
        AddProductBottomSheet(
            materials = materials,
            onDismiss = { showProductSheet = false },
            onConfirm = { name, expectedPrice, mats ->
                viewModel.addProduct(name, expectedPrice, mats)
                showProductSheet = false
            }
        )
    }

    editingProduct?.let { product ->
        AddProductBottomSheet(
            materials      = materials,
            initialProduct = product,
            onDismiss      = { editingProduct = null },
            onConfirm      = { name, expectedPrice, newMats ->
                viewModel.updateProduct(product.product.id, name, expectedPrice, product.materials, newMats)
                editingProduct = null
            }
        )
    }

    // ── Material sheets ────────────────────────────────────────────────────────

    if (showMaterialSheet) {
        AddMaterialBottomSheet(
            onDismiss = { showMaterialSheet = false },
            onConfirm = { name, cost, qty, unit ->
                viewModel.addMaterial(name, cost, qty, unit)
                showMaterialSheet = false
            }
        )
    }

    editingMaterial?.let { material ->
        AddMaterialBottomSheet(
            initialMaterial = material,
            onDismiss       = { editingMaterial = null },
            onConfirm       = { name, cost, qty, unit ->
                viewModel.updateMaterial(
                    MaterialEntity(
                        id       = material.id,
                        name     = name,
                        cost     = cost,
                        quantity = qty,
                        unit     = unit,
                        unitCost = 0.0
                    )
                )
                editingMaterial = null
            }
        )
    }
}
