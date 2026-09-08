package com.pdm.nivelbolha

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Funcoes de calculo do nivel de bolha.
 *
 * Este arquivo nao depende de nenhuma classe do Android de proposito: assim a
 * matematica do aplicativo fica separada da interface e pode ser testada
 * isoladamente na JVM (ver app/src/test).
 */

/** Tolerancia, em graus, para considerar que o aparelho atingiu 0 grau ou 90 graus. */
const val TOLERANCIA_GRAUS = 2f

/** Inclinacao, em graus, que leva a bolha ate a borda do mostrador. */
const val INCLINACAO_MAXIMA = 30f

/** Zonas de interesse do aplicativo. FORA = nenhuma posicao notavel. */
enum class Zona { FORA, ZERO, NOVENTA }

/** Deslocamento da bolha em relacao ao centro do mostrador, em pixels. */
data class PosicaoBolha(val x: Float, val y: Float)

/** Converte um angulo de radianos (como vem do sensor) para graus. */
fun radianosParaGraus(radianos: Float): Float =
    Math.toDegrees(radianos.toDouble()).toFloat()

/**
 * Inclinacao total do aparelho em relacao ao plano horizontal.
 *
 * E o angulo entre a tela e a horizontal, combinando pitch e roll em um unico
 * valor. Nao e a soma dos dois: um aparelho em pe continua inclinado 90 graus
 * mesmo que pitch e roll valham 90 ao mesmo tempo.
 */
fun calcularInclinacaoTotal(pitch: Float, roll: Float): Float {
    val p = Math.toRadians(pitch.toDouble())
    val r = Math.toRadians(roll.toDouble())
    val cosseno = (cos(p) * cos(r)).coerceIn(-1.0, 1.0)
    return Math.toDegrees(acos(cosseno)).toFloat()
}

/** Verifica se o aparelho esta nivelado, ou seja, com pitch e roll proximos de zero. */
fun estaNivelado(pitch: Float, roll: Float, tolerancia: Float = TOLERANCIA_GRAUS): Boolean =
    abs(pitch) <= tolerancia && abs(roll) <= tolerancia

/** Verifica se o aparelho esta proximo de 90 graus em algum dos dois eixos. */
fun estaEmNoventaGraus(pitch: Float, roll: Float, tolerancia: Float = TOLERANCIA_GRAUS): Boolean =
    abs(abs(pitch) - 90f) <= tolerancia || abs(abs(roll) - 90f) <= tolerancia

/** Classifica a posicao atual do aparelho em uma das zonas conhecidas. */
fun detectarZona(pitch: Float, roll: Float, tolerancia: Float = TOLERANCIA_GRAUS): Zona = when {
    estaNivelado(pitch, roll, tolerancia) -> Zona.ZERO
    estaEmNoventaGraus(pitch, roll, tolerancia) -> Zona.NOVENTA
    else -> Zona.FORA
}

/**
 * Converte a inclinacao do aparelho no deslocamento da bolha dentro do mostrador.
 *
 * Assim como em um nivel de bolha real, a bolha caminha para o lado que esta
 * mais alto: levantar a borda direita empurra a bolha para a direita, levantar
 * a borda superior empurra a bolha para cima.
 *
 * @param raioMaximo distancia maxima que a bolha pode se afastar do centro.
 * @param inclinacaoMaxima inclinacao que ja joga a bolha na borda do mostrador.
 */
fun calcularPosicaoBolha(
    pitch: Float,
    roll: Float,
    raioMaximo: Float,
    inclinacaoMaxima: Float = INCLINACAO_MAXIMA
): PosicaoBolha {
    // Normaliza a inclinacao para a faixa -1..1 (coerceIn evita passar do limite).
    val eixoX = (-roll / inclinacaoMaxima).coerceIn(-1f, 1f)
    val eixoY = (pitch / inclinacaoMaxima).coerceIn(-1f, 1f)

    var x = eixoX * raioMaximo
    var y = eixoY * raioMaximo

    // Nas diagonais os dois eixos somados passariam da borda: encurta o vetor
    // para que a bolha nunca saia do circulo do mostrador.
    val distancia = sqrt(x * x + y * y)
    if (distancia > raioMaximo && distancia > 0f) {
        x = x / distancia * raioMaximo
        y = y / distancia * raioMaximo
    }
    return PosicaoBolha(x, y)
}

/**
 * Filtro simples para suavizar a leitura do sensor e evitar que a bolha trema.
 *
 * Saltos muito grandes (o pitch vira de +180 para -180 quando o aparelho passa
 * de cabeca para baixo) sao aceitos direto, sem suavizacao.
 */
fun suavizar(valorAtual: Float, novoValor: Float, fator: Float = 0.2f): Float =
    if (abs(novoValor - valorAtual) > 90f) novoValor
    else valorAtual + (novoValor - valorAtual) * fator

/**
 * Decide quando o aparelho deve vibrar.
 *
 * A vibracao acontece apenas na transicao para uma zona notavel (0 grau ou 90
 * graus). Enquanto o usuario permanece parado na mesma zona, [atualizar]
 * devolve false e o aparelho nao vibra de novo a cada leitura do sensor.
 *
 * Para sair de uma zona e preciso passar de uma tolerancia um pouco maior do
 * que a usada para entrar (histerese). Isso impede que o ruido do sensor, bem
 * na fronteira da faixa, faca o aparelho entrar e sair varias vezes por segundo.
 */
class ControleVibracao(
    private val toleranciaEntrada: Float = TOLERANCIA_GRAUS,
    private val toleranciaSaida: Float = TOLERANCIA_GRAUS + 1f
) {
    var zonaAtual: Zona = Zona.FORA
        private set

    /** Atualiza a zona e devolve true somente quando o aparelho acabou de entrar nela. */
    fun atualizar(pitch: Float, roll: Float): Boolean {
        val tolerancia = if (zonaAtual == Zona.FORA) toleranciaEntrada else toleranciaSaida
        val novaZona = detectarZona(pitch, roll, tolerancia)
        val entrouAgora = novaZona != zonaAtual && novaZona != Zona.FORA
        zonaAtual = novaZona
        return entrouAgora
    }

    /** Volta ao estado inicial (usado quando o sensor e religado no onResume). */
    fun reiniciar() {
        zonaAtual = Zona.FORA
    }
}
