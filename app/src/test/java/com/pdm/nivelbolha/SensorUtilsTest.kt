package com.pdm.nivelbolha

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

/**
 * Testes da logica de calculo do nivel de bolha.
 *
 * Rodam na JVM (./gradlew test), sem precisar de emulador ou aparelho.
 */
class SensorUtilsTest {

    // ---------- Conversao de radianos para graus ----------

    @Test
    fun converteRadianosParaGraus() {
        assertEquals(0f, radianosParaGraus(0f), 0.001f)
        assertEquals(180f, radianosParaGraus(Math.PI.toFloat()), 0.01f)
        assertEquals(-90f, radianosParaGraus((-Math.PI / 2).toFloat()), 0.01f)
    }

    // ---------- Inclinacao total ----------

    @Test
    fun aparelhoDeitadoTemInclinacaoZero() {
        assertEquals(0f, calcularInclinacaoTotal(0f, 0f), 0.01f)
    }

    @Test
    fun aparelhoEmPeTemInclinacaoDeNoventaGraus() {
        assertEquals(90f, calcularInclinacaoTotal(90f, 0f), 0.01f)
        assertEquals(90f, calcularInclinacaoTotal(0f, 90f), 0.01f)
        // Mesmo com os dois eixos em 90 o aparelho continua a 90 graus da horizontal.
        assertEquals(90f, calcularInclinacaoTotal(90f, 90f), 0.01f)
    }

    @Test
    fun inclinacaoEmUmUnicoEixoEIgualAoAngulo() {
        assertEquals(30f, calcularInclinacaoTotal(30f, 0f), 0.01f)
        assertEquals(15f, calcularInclinacaoTotal(0f, -15f), 0.01f)
    }

    // ---------- Deteccao de 0 grau ----------

    @Test
    fun reconheceAparelhoNivelado() {
        assertTrue(estaNivelado(0f, 0f))
        assertTrue(estaNivelado(1.5f, -1.9f))
        assertTrue(estaNivelado(2f, 2f))
    }

    @Test
    fun reconheceAparelhoInclinado() {
        assertFalse(estaNivelado(3f, 0f))
        assertFalse(estaNivelado(0f, -5f))
        assertFalse(estaNivelado(45f, 45f))
    }

    // ---------- Deteccao de 90 graus ----------

    @Test
    fun reconheceNoventaGrausDentroDaTolerancia() {
        assertTrue(estaEmNoventaGraus(88f, 0f))
        assertTrue(estaEmNoventaGraus(90f, 0f))
        assertTrue(estaEmNoventaGraus(92f, 0f))
        assertTrue(estaEmNoventaGraus(-90f, 0f))
        assertTrue(estaEmNoventaGraus(0f, 89f))
    }

    @Test
    fun naoConfundeOutrosAngulosComNoventaGraus() {
        assertFalse(estaEmNoventaGraus(85f, 0f))
        assertFalse(estaEmNoventaGraus(0f, 0f))
        assertFalse(estaEmNoventaGraus(45f, 45f))
    }

    @Test
    fun classificaAsZonasCorretamente() {
        assertEquals(Zona.ZERO, detectarZona(0f, 0f))
        assertEquals(Zona.NOVENTA, detectarZona(-90f, 0f))
        assertEquals(Zona.FORA, detectarZona(30f, 10f))
    }

    // ---------- Posicao da bolha ----------

    @Test
    fun bolhaFicaNoCentroQuandoNivelado() {
        val posicao = calcularPosicaoBolha(0f, 0f, raioMaximo = 100f)
        assertEquals(0f, posicao.x, 0.001f)
        assertEquals(0f, posicao.y, 0.001f)
    }

    @Test
    fun bolhaVaiParaOLadoMaisAlto() {
        // Roll negativo = borda direita levantada -> a bolha sobe para a direita.
        assertTrue(calcularPosicaoBolha(0f, -10f, 100f).x > 0f)
        assertTrue(calcularPosicaoBolha(0f, 10f, 100f).x < 0f)
        // Pitch negativo = borda superior levantada -> bolha para cima (y negativo no Canvas).
        assertTrue(calcularPosicaoBolha(-10f, 0f, 100f).y < 0f)
        assertTrue(calcularPosicaoBolha(10f, 0f, 100f).y > 0f)
    }

    @Test
    fun bolhaSeDeslocaProporcionalmente() {
        // Metade da inclinacao maxima (30 graus) desloca metade do raio.
        val posicao = calcularPosicaoBolha(15f, 0f, raioMaximo = 100f)
        assertEquals(50f, posicao.y, 0.01f)
    }

    @Test
    fun bolhaNuncaUltrapassaOLimiteDoMostrador() {
        val raio = 100f
        val angulos = listOf(-180f, -90f, -45f, 0f, 45f, 90f, 180f)
        for (pitch in angulos) {
            for (roll in angulos) {
                val posicao = calcularPosicaoBolha(pitch, roll, raio)
                val distancia = sqrt(posicao.x * posicao.x + posicao.y * posicao.y)
                assertTrue(
                    "bolha saiu do mostrador em pitch=$pitch roll=$roll (dist=$distancia)",
                    distancia <= raio + 0.01f
                )
            }
        }
    }

    @Test
    fun naDiagonalABolhaEncostaNaBordaSemPassar() {
        val posicao = calcularPosicaoBolha(90f, 90f, raioMaximo = 100f)
        val distancia = sqrt(posicao.x * posicao.x + posicao.y * posicao.y)
        assertEquals(100f, distancia, 0.01f)
    }

    // ---------- Suavizacao ----------

    @Test
    fun suavizacaoAproximaOValorAosPoucos() {
        assertEquals(2f, suavizar(0f, 10f, fator = 0.2f), 0.001f)
    }

    @Test
    fun suavizacaoAceitaSaltoGrandeDeUmaVez() {
        // Virada de +180 para -180: aceita direto, sem varrer a tela.
        assertEquals(-179f, suavizar(179f, -179f, fator = 0.2f), 0.001f)
    }

    // ---------- Controle da vibracao ----------

    @Test
    fun vibraApenasAoEntrarNaFaixaDeZeroGraus() {
        val controle = ControleVibracao()

        // Comeca inclinado: nao vibra.
        assertFalse(controle.atualizar(40f, 20f))

        // Entra na faixa de 0 grau: vibra uma vez.
        assertTrue(controle.atualizar(0.5f, 0.5f))

        // Continua parado dentro da faixa: NAO vibra de novo.
        assertFalse(controle.atualizar(0.6f, 0.4f))
        assertFalse(controle.atualizar(1.0f, -1.0f))
        assertFalse(controle.atualizar(0f, 0f))
    }

    @Test
    fun vibraNovamenteDepoisDeSairEVoltar() {
        val controle = ControleVibracao()

        assertTrue(controle.atualizar(0f, 0f))     // entrou -> vibra
        assertFalse(controle.atualizar(30f, 30f))  // saiu   -> nao vibra
        assertTrue(controle.atualizar(0f, 0f))     // voltou -> vibra
    }

    @Test
    fun vibraAoAtingirNoventaGraus() {
        val controle = ControleVibracao()

        assertFalse(controle.atualizar(45f, 0f))
        assertTrue(controle.atualizar(89.5f, 0f))
        assertFalse(controle.atualizar(90f, 0f))
    }

    @Test
    fun histereseEvitaVibrarSemPararNaFronteiraDaFaixa() {
        val controle = ControleVibracao()

        assertTrue(controle.atualizar(1.9f, 0f))
        // Ruido do sensor em volta de 2 graus: continua na zona, sem vibrar de novo.
        assertFalse(controle.atualizar(2.1f, 0f))
        assertFalse(controle.atualizar(1.8f, 0f))
        assertFalse(controle.atualizar(2.4f, 0f))
        assertFalse(controle.atualizar(2.0f, 0f))
        assertEquals(Zona.ZERO, controle.zonaAtual)
    }

    @Test
    fun reiniciarVoltaParaOEstadoInicial() {
        val controle = ControleVibracao()
        assertTrue(controle.atualizar(0f, 0f))
        assertEquals(Zona.ZERO, controle.zonaAtual)

        controle.reiniciar()
        assertEquals(Zona.FORA, controle.zonaAtual)
        // Depois de reiniciar, voltar a tela nivelada vibra outra vez.
        assertTrue(controle.atualizar(0f, 0f))
    }
}
