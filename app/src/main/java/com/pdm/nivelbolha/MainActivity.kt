package com.pdm.nivelbolha

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Tela unica do aplicativo.
 *
 * A Activity conversa com o sensor e guarda os valores em estados do Compose;
 * a interface (NivelBolhaScreen) apenas le esses estados e se redesenha
 * sozinha sempre que eles mudam.
 */
class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensorRotacao: Sensor? = null
    private var vibrador: Vibrator? = null

    /** Guarda a zona anterior para nao vibrar a cada leitura do sensor. */
    private val controleVibracao = ControleVibracao()

    // Buffers reaproveitados a cada leitura, para nao alocar memoria no onSensorChanged.
    private val matrizRotacao = FloatArray(9)
    private val orientacao = FloatArray(3)

    // Estados observados pelo Compose: mudou o valor, a tela se redesenha.
    private var pitch by mutableFloatStateOf(0f)
    private var roll by mutableFloatStateOf(0f)
    private var zona by mutableStateOf(Zona.FORA)
    private var sensorDisponivel by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtem o servico de sensores e o sensor de vetor de rotacao do aparelho.
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorRotacao = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorDisponivel = sensorRotacao != null

        vibrador = obterVibrador()

        setContent {
            NivelBolhaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (sensorDisponivel) {
                        NivelBolhaScreen(pitch = pitch, roll = roll, zona = zona)
                    } else {
                        // Aparelho sem o sensor: mostra um aviso em vez de quebrar.
                        SensorIndisponivelScreen()
                    }
                }
            }
        }
    }

    /** Religa o sensor quando a tela volta a ficar visivel. */
    override fun onResume() {
        super.onResume()
        val sensor = sensorRotacao ?: return
        controleVibracao.reiniciar()
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    /** Desliga o sensor ao sair da tela, para nao gastar bateria a toa. */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor?.type != Sensor.TYPE_ROTATION_VECTOR) return

        // Alguns aparelhos entregam mais de 4 valores e a funcao abaixo rejeita
        // o vetor maior; por isso usamos apenas os 4 primeiros.
        val vetor = if (event.values.size > 4) event.values.copyOf(4) else event.values

        try {
            // Converte o vetor de rotacao na matriz de rotacao do aparelho.
            SensorManager.getRotationMatrixFromVector(matrizRotacao, vetor)
        } catch (e: IllegalArgumentException) {
            return
        }

        // Extrai azimute, pitch e roll a partir da matriz (valores em radianos).
        SensorManager.getOrientation(matrizRotacao, orientacao)

        // Converte os valores de orientacao de radianos para graus.
        val novoPitch = radianosParaGraus(orientacao[1])
        val novoRoll = radianosParaGraus(orientacao[2])
        if (novoPitch.isNaN() || novoRoll.isNaN()) return

        // Suaviza a leitura para a bolha nao ficar tremendo na tela.
        pitch = suavizar(pitch, novoPitch)
        roll = suavizar(roll, novoRoll)

        // Vibra somente quando o aparelho ACABA de entrar na faixa de 0 ou 90 graus.
        if (controleVibracao.atualizar(pitch, roll)) {
            vibrar()
        }
        zona = controleVibracao.zonaAtual
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // A precisao do vetor de rotacao nao altera a leitura; nada a fazer aqui.
    }

    /** Obtem o vibrador usando VibratorManager no Android 12 ou mais recente. */
    private fun obterVibrador(): Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val gerenciador = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            gerenciador?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    /** Dispara uma vibracao curta de feedback. */
    private fun vibrar() {
        val v = vibrador ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(120L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(120L)
            }
        } catch (e: Exception) {
            // Sem vibracao o aplicativo continua funcionando normalmente.
        }
    }
}
