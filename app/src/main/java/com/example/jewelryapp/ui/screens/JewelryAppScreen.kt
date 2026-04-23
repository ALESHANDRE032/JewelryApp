package com.example.jewelryapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.jewelryapp.JewelryViewModel
import com.example.jewelryapp.ui.dialogs.AddMaterialBottomSheet
import com.example.jewelryapp.ui.dialogs.AddSaleBottomSheet
import com.example.jewelryapp.ui.theme.GoldBg
import com.example.jewelryapp.ui.theme.GoldInk
import com.example.jewelryapp.ui.theme.Ink
import com.example.jewelryapp.ui.theme.Surface

private enum class AppTab { Home, Sales, Materials, Analytics }

@Composable
fun JewelryAppScreen(viewModel: JewelryViewModel) {
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    var showSaleSheet     by remember { mutableStateOf(false) }
    var showMaterialSheet by remember { mutableStateOf(false) }

    val sales     by viewModel.sales.collectAsState()
    val materials by viewModel.materials.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = Surface, contentColor = Ink) {
                listOf(
                    Triple(AppTab.Home,      "Главная",    Icons.Filled.Home),
                    Triple(AppTab.Sales,     "Продажи",    Icons.Filled.ShoppingCart),
                    Triple(AppTab.Materials, "Материалы",  Icons.Filled.Star),
                    Triple(AppTab.Analytics, "Аналитика",  Icons.Filled.List)
                ).forEach { (tab, label, icon) ->
                    val selected = selectedTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick  = { selectedTab = tab },
                        icon = { Icon(icon, contentDescription = label) },
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
                    sales     = sales,
                    materials = materials,
                    onAddSale     = { showSaleSheet = true },
                    onAddMaterial = { showMaterialSheet = true }
                )
                AppTab.Sales -> SalesScreen(
                    sales        = sales,
                    onDeleteSale = { viewModel.deleteSale(it) },
                    onAddSale    = { showSaleSheet = true }
                )
                AppTab.Materials -> MaterialsScreen(
                    materials     = materials,
                    onAddMaterial = { showMaterialSheet = true }
                )
                AppTab.Analytics -> AnalyticsScreen(sales = sales)
            }
        }
    }

    if (showSaleSheet) {
        AddSaleBottomSheet(
            materials = materials,
            onDismiss = { showSaleSheet = false },
            onConfirm = { name, price, channel, mats ->
                viewModel.addSale(name, price, channel, mats)
                showSaleSheet = false
            }
        )
    }

    if (showMaterialSheet) {
        AddMaterialBottomSheet(
            onDismiss = { showMaterialSheet = false },
            onConfirm = { name, cost ->
                viewModel.addMaterial(name, cost)
                showMaterialSheet = false
            }
        )
    }
}
