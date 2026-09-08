package com.pdm.nivelbolha

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/** Tela principal: titulo, angulo, mostrador da bolha, valores e estado. */
@Composable
fun NivelBolhaScreen(
    pitch: Float,
    roll: Float,
    zona: Zona,
    modifier: Modifier = Modifier
) {
    val nivelado = zona == Zona.ZERO
    val inclinacaoTotal = calcularInclinacaoTotal(pitch, roll)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        // Angulo principal, exibido em destaque com duas casas decimais.
        Text(
            text = formatarGraus(inclinacaoTotal),
            fontSize = 52.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        // Mostrador desenhado com Canvas.
        BubbleLevel(
            pitch = pitch,
            roll = roll,
            nivelado = nivelado,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
            corBolha = MaterialTheme.colorScheme.primary,
            corLinhas = MaterialTheme.colorScheme.onSurfaceVariant,
            corBorda = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(20.dp))

        // Valores de pitch e roll lado a lado.
        Row(
            modifier = Modifier.widthIn(max = 300.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CartaoAngulo(
                rotulo = stringResource(R.string.pitch),
                valor = pitch,
                modifier = Modifier.weight(1f)
            )
            CartaoAngulo(
                rotulo = stringResource(R.string.roll),
                valor = roll,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        IndicadorEstado(zona = zona)

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.dica),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Cartao com o nome e o valor de um dos angulos. */
@Composable
private fun CartaoAngulo(rotulo: String, valor: Float, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = rotulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatarGraus(valor),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Faixa que mostra NIVELADO, 90 GRAUS ou INCLINADO. */
@Composable
private fun IndicadorEstado(zona: Zona) {
    val (texto, cor, icone) = when (zona) {
        Zona.ZERO -> Triple(
            stringResource(R.string.nivelado),
            Color(0xFF2E7D32),
            Icons.Filled.CheckCircle
        )
        Zona.NOVENTA -> Triple(
            stringResource(R.string.noventa_graus),
            Color(0xFF1565C0),
            Icons.Filled.Info
        )
        Zona.FORA -> Triple(
            stringResource(R.string.inclinado),
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Filled.Warning
        )
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = cor.copy(alpha = 0.14f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = cor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = cor
            )
        }
    }
}

/** Tela exibida quando o aparelho nao possui o sensor de vetor de rotacao. */
@Composable
fun SensorIndisponivelScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.sensor_indisponivel),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.sensor_indisponivel_detalhe),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Formata um angulo com duas casas decimais, por exemplo "1,25 graus". */
private fun formatarGraus(valor: Float): String =
    String.format(Locale.getDefault(), "%.2f°", valor)

// ---------- Pre-visualizacoes do Android Studio ----------

@Preview(showBackground = true, name = "Nivelado")
@Composable
private fun PreviewNivelado() {
    NivelBolhaTheme {
        NivelBolhaScreen(pitch = 0.31f, roll = 0.42f, zona = Zona.ZERO)
    }
}

@Preview(showBackground = true, name = "Inclinado")
@Composable
private fun PreviewInclinado() {
    NivelBolhaTheme {
        NivelBolhaScreen(pitch = -12.4f, roll = 8.75f, zona = Zona.FORA)
    }
}

@Preview(showBackground = true, name = "Sem sensor")
@Composable
private fun PreviewSemSensor() {
    NivelBolhaTheme {
        SensorIndisponivelScreen()
    }
}
