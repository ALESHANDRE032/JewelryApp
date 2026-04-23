package com.example.jewelryapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary            = Gold,
    onPrimary          = Surface,
    primaryContainer   = GoldBg,
    onPrimaryContainer = GoldInk,
    secondary          = InkSoft,
    onSecondary        = Surface,
    background         = Surface,
    onBackground       = Ink,
    surface            = Surface,
    onSurface          = Ink,
    surfaceVariant     = SurfaceAlt,
    onSurfaceVariant   = InkSoft,
    surfaceContainer   = SurfaceDeep,
    outline            = Divider,
    error              = Loss,
    errorContainer     = LossBg
)

@Composable
fun JewelryAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = Typography,
        content     = content
    )
}
