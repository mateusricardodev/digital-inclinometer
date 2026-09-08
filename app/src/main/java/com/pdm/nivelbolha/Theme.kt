package com.pdm.nivelbolha

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Tema Material 3 simples do aplicativo, com versao clara e escura. */

private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFF00695C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF37474F),
    background = Color(0xFFF5F7F8),
    onBackground = Color(0xFF191C1B),
    surface = Color.White,
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFE0E4E3),
    onSurfaceVariant = Color(0xFF3F4947)
)

private val EsquemaEscuro = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005046),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFFB0BEC5),
    background = Color(0xFF101413),
    onBackground = Color(0xFFE0E3E1),
    surface = Color(0xFF191D1C),
    onSurface = Color(0xFFE0E3E1),
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBEC9C6)
)

@Composable
fun NivelBolhaTheme(
    escuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // A partir do Android 12 o sistema pode fornecer cores dinamicas do papel de parede.
    val esquema = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val contexto = LocalContext.current
            if (escuro) dynamicDarkColorScheme(contexto) else dynamicLightColorScheme(contexto)
        }
        escuro -> EsquemaEscuro
        else -> EsquemaClaro
    }

    MaterialTheme(colorScheme = esquema, content = content)
}
