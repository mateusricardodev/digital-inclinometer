# Nível de Bolha & Inclinômetro Digital

Aplicativo Android nativo (Kotlin + Jetpack Compose) que usa o sensor de vetor de
rotação do aparelho para funcionar como um nível de bolha digital.

Projeto acadêmico da disciplina de Programação para Dispositivos Móveis —
proposta 2 do documento `Projetos_PDM.pdf`.

## O que o aplicativo faz

- Lê o sensor `Sensor.TYPE_ROTATION_VECTOR` e extrai os ângulos **pitch** e **roll**.
- Mostra a inclinação total em destaque, com duas casas decimais.
- Desenha um mostrador com `Canvas`, no qual a bolha se desloca conforme a inclinação.
- Indica **NIVELADO**, **90 GRAUS** ou **INCLINADO**.
- Vibra ao entrar na faixa de 0° ou 90° — uma única vez por entrada, nunca de forma contínua.
- Exibe um aviso amigável em aparelhos que não possuem o sensor.

## Requisitos

- Android Studio (Ladybug ou mais recente)
- JDK 17 ou superior
- Android SDK 35 (`compileSdk`/`targetSdk`), `minSdk` 24

## Como executar

```bash
./gradlew installDebug     # instala em um aparelho ou emulador conectado
./gradlew test             # roda os testes de lógica na JVM
```

No Android Studio: abrir a pasta do projeto e usar *Run*.

> O emulador padrão do Android Studio já fornece o sensor de vetor de rotação.
> Os valores podem ser alterados na aba *Virtual sensors* dos *Extended controls*.
> A vibração só é sentida em um aparelho físico.

## Estrutura

```
app/src/main/java/com/pdm/nivelbolha/
├── MainActivity.kt        # Activity, leitura do sensor, ciclo de vida e vibração
├── SensorUtils.kt         # cálculos (pitch, roll, posição da bolha, zonas) — sem Android
├── BubbleLevel.kt         # mostrador desenhado com Canvas
├── NivelBolhaScreen.kt    # interface em Jetpack Compose
└── Theme.kt               # tema Material 3

app/src/test/java/com/pdm/nivelbolha/
└── SensorUtilsTest.kt     # 21 testes da lógica de cálculo
```

`SensorUtils.kt` não importa nenhuma classe do Android de propósito: assim a
matemática fica separada da interface e pode ser testada direto na JVM, sem
emulador.

## Como funciona

**Sensor.** A `MainActivity` implementa `SensorEventListener`. Em `onSensorChanged`,
`SensorManager.getRotationMatrixFromVector()` converte o vetor de rotação em uma
matriz 3x3 e `SensorManager.getOrientation()` extrai azimute, pitch e roll em
radianos, convertidos para graus com `Math.toDegrees()`.

**Bolha.** `calcularPosicaoBolha()` transforma pitch e roll em um deslocamento em
pixels. A bolha caminha para o lado mais alto, como em um nível real, e o vetor é
encurtado quando passaria da borda do mostrador.

**Vibração.** `ControleVibracao` guarda a zona atual (FORA, ZERO, NOVENTA) e só
devolve `true` na transição de entrada em uma zona. Para sair de uma zona é preciso
passar de uma tolerância um pouco maior do que a de entrada (histerese), o que impede
que o ruído do sensor faça o aparelho vibrar sem parar na fronteira da faixa.

**Ciclo de vida.** O listener é registrado em `onResume()` e removido em `onPause()`,
para não consumir bateria com a tela em segundo plano.

## Tolerâncias

| Constante            | Valor | Onde é usada                                  |
|----------------------|-------|-----------------------------------------------|
| `TOLERANCIA_GRAUS`   | 2°    | faixa de 0° e de 90°                          |
| `INCLINACAO_MAXIMA`  | 30°   | inclinação que leva a bolha até a borda       |

Ambas ficam em `SensorUtils.kt` e podem ser ajustadas em um único lugar.
