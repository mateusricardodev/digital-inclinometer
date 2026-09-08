# 📐 Nível de Bolha & Inclinômetro Digital

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white" alt="Android Studio">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
</p>

<p align="center">
  <strong>Um nível de bolha digital desenvolvido para dispositivos Android utilizando sensores do próprio aparelho.</strong>
</p>

---

## 📱 Sobre o projeto

O **Nível de Bolha & Inclinômetro Digital** é um aplicativo Android desenvolvido em **Kotlin** com o objetivo de transformar o smartphone em uma ferramenta digital para medição de inclinação.

O aplicativo utiliza os sensores internos do dispositivo para identificar a orientação do aparelho e calcular os ângulos de **Pitch** e **Roll**, apresentando essas informações de maneira visual e intuitiva.

A interface conta com um mostrador dinâmico que representa uma bolha digital, permitindo que o usuário visualize em tempo real a inclinação do aparelho.

O projeto faz parte da disciplina de **Programação para Dispositivos Móveis (PDM)** e tem como foco a utilização da API nativa de sensores do Android.

---

## 🎯 Objetivos

* Desenvolver um aplicativo Android utilizando Kotlin;
* Trabalhar com os sensores nativos do dispositivo;
* Utilizar o `SensorManager`;
* Identificar a orientação do aparelho;
* Calcular os ângulos de inclinação;
* Representar os dados através de uma interface gráfica;
* Implementar um nível de bolha digital;
* Trabalhar com feedback tátil através de vibração;
* Aplicar conceitos de desenvolvimento nativo para Android.

---

## ⚙️ Funcionalidades

### 📐 Inclinômetro

O aplicativo calcula e apresenta os dois principais ângulos de orientação utilizados no projeto:

* **Pitch**
* **Roll**

Os valores são atualizados conforme o usuário movimenta o aparelho.

### 🫧 Nível de bolha digital

A aplicação possui um mostrador gráfico que representa uma bolha.

Conforme o smartphone é inclinado, a posição da bolha é alterada de acordo com a orientação detectada pelos sensores.

### 🎯 Detecção de nível

O aplicativo identifica quando o dispositivo está próximo de uma posição nivelada.

Quando os valores estão próximos de `0°`, o usuário recebe uma indicação visual de que o aparelho está nivelado.

### 🔄 Detecção de 90°

O sistema também considera a posição aproximada de `90°`, conforme especificado na proposta do projeto.

### 📳 Feedback tátil

Quando uma posição de referência é atingida, o aplicativo utiliza vibração como feedback para o usuário.

A proposta original especifica feedback tátil quando a precisão se aproxima de **0° ou 90°**.

---

## 🧭 Funcionamento

O funcionamento básico do aplicativo pode ser representado pelo seguinte fluxo:

```text
┌──────────────────────┐
│ Usuário movimenta    │
│ o smartphone         │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Sensor de rotação    │
│ do dispositivo       │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ SensorManager        │
│ SensorEventListener  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Cálculo da orientação│
│ Pitch / Roll         │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Interface atualizada │
│ em tempo real        │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Canvas movimenta     │
│ a bolha digital      │
└──────────────────────┘
```

---

## 🧠 Conceitos técnicos

O projeto utiliza a API nativa de sensores do Android.

### `SensorManager`

Responsável pelo gerenciamento dos sensores disponíveis no dispositivo.

```kotlin
val sensorManager =
    getSystemService(Context.SENSOR_SERVICE) as SensorManager
```

### `TYPE_ROTATION_VECTOR`

O sensor principal utilizado pelo projeto é:

```kotlin
Sensor.TYPE_ROTATION_VECTOR
```

Ele fornece informações utilizadas para determinar a orientação do dispositivo.

---

## 📊 Cálculo da orientação

Para obter a orientação do aparelho, o projeto utiliza recursos disponibilizados pelo `SensorManager`.

Entre eles:

```kotlin
SensorManager.getRotationMatrixFromVector()
```

e:

```kotlin
SensorManager.getOrientation()
```

Os valores obtidos são utilizados para calcular os ângulos de:

```text
Pitch
Roll
```

Como os valores de orientação são obtidos em radianos, eles são convertidos para graus para facilitar a visualização pelo usuário.

---

## 🎨 Interface

A interface do aplicativo é desenvolvida utilizando **Jetpack Compose**.

O principal elemento visual é um `Canvas`, utilizado para desenhar o nível de bolha e atualizar sua posição conforme os valores dos sensores.

Exemplo conceitual:

```text
              NÍVEL DE BOLHA

                  0.42°

             ┌───────────┐
             │           │
             │     ●     │
             │     +     │
             │           │
             └───────────┘

             Pitch: 0.31°
             Roll:  0.42°

              ✓ NIVELADO
```

---

## 📳 Feedback por vibração

O aplicativo utiliza o sistema de vibração do Android para fornecer feedback tátil.

O conceito utilizado no projeto é o:

```kotlin
VibratorManager
```

A vibração é acionada quando o aparelho entra em uma das zonas de referência configuradas.

Para evitar vibrações contínuas, o sistema deve controlar quando o usuário entra ou sai da zona de referência.

---

## 🔋 Ciclo de vida

O monitoramento dos sensores é integrado ao ciclo de vida da Activity.

Ao retornar para o aplicativo:

```kotlin
onResume()
```

o sensor é registrado novamente.

Ao sair ou pausar:

```kotlin
onPause()
```

o listener é removido.

Isso evita manter o sensor funcionando quando o aplicativo não está sendo utilizado.

---

## 🛠️ Tecnologias utilizadas

| Tecnologia         | Utilização                  |
| ------------------ | --------------------------- |
| ☕ Kotlin           | Linguagem de programação    |
| 🤖 Android         | Plataforma                  |
| 🧩 Android Studio  | Ambiente de desenvolvimento |
| 📐 SensorManager   | Gerenciamento dos sensores  |
| 🔄 Rotation Vector | Orientação do dispositivo   |
| 🎨 Jetpack Compose | Construção da interface     |
| 🖌️ Canvas         | Desenho do nível de bolha   |
| 📳 VibratorManager | Feedback tátil              |

A proposta do projeto especifica o desenvolvimento nativo com **Kotlin e Android Studio**, utilizando a API de sensores do Android.

---

## 📂 Estrutura do projeto

Uma possível organização:

```text
NiveDeBolha/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── .../
│           │       └── MainActivity.kt
│           │
│           ├── res/
│           │   ├── drawable/
│           │   ├── mipmap/
│           │   └── values/
│           │
│           └── AndroidManifest.xml
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## ▶️ Como executar

### 1. Clone o repositório

```bash
git clone SEU_REPOSITORIO
```

### 2. Abra o projeto

Abra a pasta do projeto no:

**Android Studio**

### 3. Aguarde a sincronização

Aguarde o Android Studio concluir o **Gradle Sync**.

### 4. Conecte um dispositivo

Utilize:

* um smartphone Android físico; ou
* um emulador que possua suporte aos sensores necessários.

> Para testar corretamente a funcionalidade de inclinação, recomenda-se utilizar um dispositivo físico com sensores compatíveis.

### 5. Execute

Clique em:

```text
▶ Run
```

ou utilize:

```text
Shift + F10
```

---

## 📱 Compatibilidade

O funcionamento do aplicativo depende da presença dos sensores necessários no dispositivo.

Antes de utilizar o sensor, o aplicativo deve verificar sua disponibilidade.

Caso o sensor não esteja disponível, deve apresentar uma mensagem informando ao usuário que o dispositivo não possui o recurso necessário.

---

## 🧪 Testes

### Testes de unidade

A lógica de cálculo fica isolada em `SensorUtils.kt`, que não importa nenhuma classe do Android. Isso permite testá-la direto na JVM, sem emulador nem aparelho:

```bash
./gradlew test
```

São **21 testes** em `app/src/test/java/com/pdm/nivelbolha/SensorUtilsTest.kt`:

| Grupo | O que é verificado |
| ----- | ------------------ |
| Conversão de ângulos | Radianos para graus; inclinação total combinando pitch e roll |
| Detecção de 0° | Nivelado dentro da tolerância de 2°; inclinado fora dela |
| Detecção de 90° | 88°, 90° e 92° reconhecidos; 45° e 85° recusados |
| Posição da bolha | Centro quando nivelado; deslocamento proporcional; sentido correto |
| Limite do mostrador | Varredura de −180° a 180° nos dois eixos — a bolha nunca sai do círculo |
| Suavização | Aproximação gradual do valor e salto aceito na virada de ±180° |
| Controle da vibração | Vibra ao entrar na zona; não repete com o aparelho parado; volta a vibrar após sair e retornar; histerese na fronteira da faixa |

**Resultado: 21/21 aprovados.**

### Testes no dispositivo

Executados em um **Samsung Galaxy A56 (SM-A566E)** físico, com o aplicativo instalado via Android Studio.

| # | Teste | Verificação | Resultado |
| - | ----- | ----------- | --------- |
| 1 | Inicialização | O aplicativo abre sem erros | ✅ |
| 2 | Sensor | O sensor de rotação é identificado corretamente | ✅ |
| 3 | Pitch | Inclinar o aparelho para frente e para trás altera o valor | ✅ |
| 4 | Roll | Girar o aparelho lateralmente altera o valor | ✅ |
| 5 | Nível | Sobre superfície plana: Pitch ≈ 0°, Roll ≈ 0° e status `NIVELADO` | ✅ |
| 6 | Inclinação | Ao inclinar: status `INCLINADO` e a bolha se desloca | ✅ |
| 7 | 90 graus | Aparelho em pé é reconhecido como posição de 90° | ✅ |
| 8 | Vibração | Há feedback tátil ao entrar na faixa de 0° ou 90° | ✅ |
| 9 | Vibração repetida | Parado na mesma faixa, o aparelho **não** vibra de novo | ✅ |
| 10 | Ciclo de vida | Sair da Activity e retornar mantém o sensor funcionando | ✅ |

**Resultado: 10/10 aprovados.**

---

## 📚 Contexto acadêmico

Este aplicativo foi desenvolvido como projeto da disciplina de:

**Programação para Dispositivos Móveis — PDM**

A proposta escolhida no material da disciplina é o **Nível de Bolha & Inclinômetro Digital**, uma ferramenta destinada a carpintaria, reformas e alinhamento de superfícies.

O projeto tem como objetivo colocar em prática o desenvolvimento nativo para Android e a utilização dos sensores disponíveis no dispositivo.

---

## 🚀 Possíveis melhorias

Algumas funcionalidades podem ser adicionadas futuramente:

* [ ] Histórico das medições;
* [ ] Calibração manual do sensor;
* [ ] Modo escuro;
* [ ] Mais opções de visualização;
* [ ] Animações adicionais;
* [ ] Configuração da tolerância de nivelamento;
* [ ] Diferentes estilos de nível de bolha.

---

## 👨‍💻 Desenvolvedor

**Mateus Ricardo**

GitHub: [@mateusricardodev](https://github.com/mateusricardodev)

---

<p align="center">

📐 **Nível de Bolha & Inclinômetro Digital**

Desenvolvido com Kotlin + Android

</p>
