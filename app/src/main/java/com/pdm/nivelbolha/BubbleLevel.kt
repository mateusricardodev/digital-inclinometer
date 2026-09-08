package com.pdm.nivelbolha

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Mostrador do nivel de bolha, desenhado com Canvas.
 *
 * O desenho e formado por: circulo externo, linhas de referencia, circulo
 * central que marca a faixa de tolerancia, uma mira no centro e a bolha, que
 * se desloca conforme o pitch e o roll.
 */
@Composable
fun BubbleLevel(
    pitch: Float,
    roll: Float,
    nivelado: Boolean,
    modifier: Modifier = Modifier,
    corBolha: Color = Color(0xFF2E7D32),
    corBolhaNivelada: Color = Color(0xFF00C853),
    corLinhas: Color = Color(0xFF9E9E9E),
    corBorda: Color = Color(0xFF546E7A)
) {
    // Anima a mudanca de cor para o estado nivelado ficar mais visivel.
    val destaque by animateFloatAsState(
        targetValue = if (nivelado) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "destaque"
    )

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val centro = Offset(size.width / 2f, size.height / 2f)
        val raioExterno = size.minDimension / 2f - 6.dp.toPx()
        val raioBolha = raioExterno * 0.16f
        // A bolha caminha ate encostar por dentro da borda, sem vazar.
        val raioMaximo = raioExterno - raioBolha - 2.dp.toPx()

        // Circulo externo do mostrador.
        drawCircle(
            color = corBorda,
            radius = raioExterno,
            center = centro,
            style = Stroke(width = 3.dp.toPx())
        )

        // Circulo intermediario, apenas como referencia visual.
        drawCircle(
            color = corLinhas.copy(alpha = 0.4f),
            radius = raioExterno * 0.6f,
            center = centro,
            style = Stroke(width = 1.dp.toPx())
        )

        // Linhas de referencia horizontal e vertical.
        drawLine(
            color = corLinhas.copy(alpha = 0.5f),
            start = Offset(centro.x - raioExterno, centro.y),
            end = Offset(centro.x + raioExterno, centro.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = corLinhas.copy(alpha = 0.5f),
            start = Offset(centro.x, centro.y - raioExterno),
            end = Offset(centro.x, centro.y + raioExterno),
            strokeWidth = 1.dp.toPx()
        )

        // Circulo central: representa a faixa considerada nivelada.
        val raioAlvo = raioMaximo * (TOLERANCIA_GRAUS / INCLINACAO_MAXIMA)
        drawCircle(
            color = if (nivelado) corBolhaNivelada else corLinhas,
            radius = raioAlvo.coerceAtLeast(raioBolha * 0.6f),
            center = centro,
            style = Stroke(width = 2.dp.toPx())
        )

        // Mira "+" no centro exato do mostrador.
        val mira = raioExterno * 0.08f
        drawLine(
            color = corLinhas,
            start = Offset(centro.x - mira, centro.y),
            end = Offset(centro.x + mira, centro.y),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = corLinhas,
            start = Offset(centro.x, centro.y - mira),
            end = Offset(centro.x, centro.y + mira),
            strokeWidth = 2.dp.toPx()
        )

        // Posicao da bolha calculada a partir da inclinacao do aparelho.
        val posicao = calcularPosicaoBolha(pitch, roll, raioMaximo)
        val centroBolha = Offset(centro.x + posicao.x, centro.y + posicao.y)

        // Halo suave em volta da bolha quando o aparelho esta nivelado.
        if (destaque > 0f) {
            drawCircle(
                color = corBolhaNivelada.copy(alpha = 0.25f * destaque),
                radius = raioBolha * 1.8f,
                center = centroBolha
            )
        }

        val cor = lerpCor(corBolha, corBolhaNivelada, destaque)
        drawCircle(color = cor, radius = raioBolha, center = centroBolha)
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = raioBolha,
            center = centroBolha,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/** Mistura duas cores conforme um fator de 0 a 1. */
private fun lerpCor(inicio: Color, fim: Color, fator: Float): Color {
    val f = fator.coerceIn(0f, 1f)
    return Color(
        red = inicio.red + (fim.red - inicio.red) * f,
        green = inicio.green + (fim.green - inicio.green) * f,
        blue = inicio.blue + (fim.blue - inicio.blue) * f,
        alpha = 1f
    )
}
