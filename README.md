<div align="center">

<img src="src/main/resources/cr/ac/una/astroline/resource/logo.png" alt="AstroLine Logo" width="120"/>

# AstroLine

**Sistema de gestión y control de fichas de atención**

[![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25.0.2-blue?style=flat-square)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.8-red?style=flat-square&logo=apachemaven)](https://maven.apache.org/)
[![MaterialFX](https://img.shields.io/badge/MaterialFX-11.16.1-purple?style=flat-square)](https://github.com/palexdev/MaterialFX)
[![AtlantaFX](https://img.shields.io/badge/AtlantaFX-2.1.0-teal?style=flat-square)](https://github.com/mkpaz/atlantafx)
[![PDFBox](https://img.shields.io/badge/PDFBox-3.x-red?style=flat-square)](https://pdfbox.apache.org/)
[![License](https://img.shields.io/badge/Licencia-Académica-green?style=flat-square)]()

*Tarea Programada — Programación II · Universidad Nacional de Costa Rica*  
*Sede Regional Brunca, Campus Pérez Zeledón · 2026*

</div>

---

## 📋 Descripción

AstroLine es una aplicación de escritorio desarrollada en JavaFX que permite la **asignación y control de fichas de atención** para empresas con múltiples sucursales y estaciones de servicio. El sistema opera en **red local (LAN)** con sincronización automática P2P entre equipos, sin necesidad de servidor central.

Cada máquina puede correr en uno de cuatro modos independientes según el rol asignado: administración, kiosko de autoatención, estación de funcionario o pantalla pública de proyección.

---

## 🎭 Módulos del sistema

| Módulo | Argumento de arranque | Descripción |
|---|---|---|
| 🛡️ **Administrador** | `admin` | Gestión completa de empresa, funcionarios, trámites, sucursales, estaciones y estadísticas. Login con credenciales de administrador. |
| 🖥️ **Kiosko** | `kiosko` | Pantalla táctil de autoatención. Teclado numérico integrado, selección de trámite, identificación opcional por cédula, atención preferencial por PIN y generación automática de ficha en PDF. |
| 👔 **Funcionario** | `funcionario` | Pantalla del agente de atención. Llamado de siguiente ficha, ficha preferencial, repetir llamado, marcar ausente y selección manual de ficha. Muestra foto y datos del cliente identificado. Login con credenciales de funcionario o admin. |
| 📺 **Proyección** | `proyeccion` | Pantalla pública. Muestra la ficha actual siendo atendida, historial de las 4 anteriores, reloj en tiempo real, texto de aviso configurable en scroll animado y anuncio de voz (TTS nativo del SO). |

> Si se ejecuta **sin argumentos**, se muestra la pantalla de selección de rol.

---

## ✨ Funcionalidades destacadas

**Sistema de fichas**
- Ciclo de 50 fichas diarias (letras A–E × números 001–010). Al completar el ciclo reinicia en A-001 automáticamente.
- Archivado automático al historial al iniciar un nuevo día.
- Detección automática de atención preferencial para mayores de 65 años (por cédula).
- Atención preferencial manual mediante PIN de administrador en el Kiosko.

**PDF de ficha (Kiosko)**
- Generado con Apache PDFBox 3.x en formato ticket compacto (3.5" × 5.3").
- Diseño cosmos: fondo oscuro con franja cian, constelación decorativa, número de ficha grande, sección de cliente y badge preferencial.
- Logo de la empresa cargado dinámicamente desde `data/logoEmpresa/`.
- Apertura automática en el visor del sistema operativo con envío a impresora.

**Sincronización P2P (LAN)**
- Descubrimiento de equipos por **UDP broadcast** (puerto 9090) sin configuración manual.
- Servidor HTTP embebido (puerto 8080) para transferencia de archivos JSON e imágenes.
- Merge con resolución de conflictos basada en `lastModified` (gana el cambio más reciente).
- Tombstones para eliminaciones: los registros borrados se propagan a todos los peers antes de limpiarse localmente.
- Polling automático cada 15 segundos + redescubrimiento de peers cada 60 segundos.
- El archivo `configuracion.json` está **excluido** del P2P: es por equipo, no compartido.

**Módulo Administrador**
- CRUD completo de empresa, funcionarios, trámites, sucursales y estaciones.
- Asignación de trámites a estaciones por **drag and drop** entre dos tablas.
- Búsqueda en vivo en todas las listas.
- Registro de clientes con foto: subida desde archivo o captura directa por **cámara web**.
- Pantalla de estadísticas con LineChart, PieChart y rankings de clientes y trámites (filtro por período y por sucursal).

**Módulo Proyección**
- Anuncio de voz con TTS nativo: PowerShell en Windows, `say` en macOS, `espeak-ng` en Linux.
- Texto de aviso en scroll horizontal (marquee) animado con velocidad configurable.
- Polling de fichas cada 1 segundo para actualización en tiempo real.

---

## 🛠️ Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 25 | Lenguaje principal |
| JavaFX | 25.0.2 | Interfaz gráfica |
| Maven | 3.8+ | Gestión de dependencias y build |
| Gson | 2.10.1 | Persistencia de datos en JSON |
| MaterialFX | 11.16.1 | Componentes UI modernos |
| AtlantaFX | 2.1.0 | Sistema de temas visuales |
| Apache PDFBox | 3.x | Generación de fichas en PDF |
| Webcam Capture | — | Captura de foto de clientes |
| JDK HTTP Server | (JDK) | Servidor HTTP embebido para P2P |
| SceneBuilder | — | Diseño de vistas FXML |
| NetBeans | — | Entorno de desarrollo recomendado |

---

## 🏗️ Arquitectura

El proyecto sigue el patrón **MVC (Model-View-Controller)** con servicios singleton y navegación centralizada via `FlowController`.

```
src/
└── main/
    ├── java/cr/ac/una/astroline/
    │   ├── App.java               ← Punto de entrada, lee arg de modo
    │   ├── controller/            ← Un controller por vista FXML
    │   ├── model/                 ← Entidades (Ficha, Cliente, Sucursal...)
    │   │                             + DTOs para binding JavaFX
    │   ├── service/               ← Lógica de negocio y CRUD (singletons)
    │   │   ├── FichaService
    │   │   ├── ClienteService
    │   │   ├── EmpresaService
    │   │   ├── SucursalService
    │   │   ├── TramiteService
    │   │   ├── FuncionarioService
    │   │   ├── ConfiguracionService
    │   │   ├── EstadisticasService
    │   │   ├── PdfService
    │   │   └── AudioService
    │   └── util/
    │       ├── FlowController     ← Navegación entre vistas
    │       ├── GsonUtil           ← Lectura/escritura JSON
    │       ├── DataInitializer    ← Inicialización de datos al primer arranque
    │       ├── DataNotifier       ← Bus de eventos para reactividad P2P
    │       ├── SyncManager        ← Orquestador del sistema P2P
    │       ├── SyncServer         ← Servidor HTTP embebido
    │       ├── SyncClient         ← Cliente HTTP para hablar con peers
    │       ├── NetworkPeer        ← Descubrimiento UDP broadcast
    │       ├── SessionManager     ← Sesión activa del funcionario
    │       ├── AppContext         ← Contexto compartido entre vistas
    │       └── Respuesta          ← Objeto de respuesta estándar de servicios
    └── resources/cr/ac/una/astroline/
        ├── view/                  ← 17 archivos FXML
        ├── resource/              ← Imágenes, íconos, fuentes, audio
        └── styles/                ← Hoja de estilo CSS
```

---

## 👥 Equipo de desarrollo

| Desarrollador | Rol | Módulo principal |
|---|---|---|
| **Johan Danilo** | Líder del proyecto | Kiosko · Proyección · Arquitectura · P2P · PDF |
| **José** (`takka_sama`) | Desarrollador | Administrador |
| **Jessica** | Desarrolladora | Funcionario |

---

## 📦 Requisitos

- **JDK 25**
- **Apache NetBeans** (recomendado) o cualquier IDE compatible con Maven
- **Maven 3.8+**
- **SceneBuilder** (opcional, para editar vistas FXML)

---

## 🚀 Ejecución

### Con Maven (modo selección de rol)

```bash
# Clonar el repositorio
git clone https://github.com/JohanDanilo/AstroLine.git
cd AstroLine

# Ejecutar sin modo (muestra pantalla de selección)
mvn javafx:run
```

### Con JAR (modos específicos por argumento)

```bash
# Construir el fat JAR
mvn package

# Cada equipo arranca en su modo correspondiente:
java -jar target/AstroLine.jar kiosko
java -jar target/AstroLine.jar funcionario
java -jar target/AstroLine.jar admin
java -jar target/AstroLine.jar proyeccion
```

> Los accesos directos `.lnk` son generados por `setup.bat` para facilitar la distribución en Windows sin necesidad de abrir una terminal.

### Credenciales por defecto (primer arranque)

| Campo | Valor |
|---|---|
| Usuario | `admin` |
| Contraseña | `1234` |
| PIN de Kiosko | `1234` |

---

## 📁 Estructura de datos

Al ejecutarse por primera vez, `DataInitializer` crea automáticamente la carpeta `data/` con todos los archivos necesarios:

```
data/
├── empresa.json          ← Nombre, logo, teléfono, correo, PIN admin
├── tramites.json         ← Catálogo de trámites (A, B, C...)
├── sucursales.json       ← Sucursales y sus estaciones embebidas
├── funcionarios.json     ← Funcionarios y administradores
├── clientes.json         ← Clientes registrados
├── fichas.json           ← Fichas activas del día actual
├── historial.json        ← Fichas archivadas de días anteriores
├── configuracion.json    ← Config local de esta máquina (no se comparte por P2P)
├── fotos/                ← Fotos de clientes
│   └── Cliente_<cedula>.png
└── logoEmpresa/          ← Logo cargado desde el módulo admin
    └── logo_empresa.png
```

Las fichas del día se archivan automáticamente en `historial.json` cuando se detecta que el día cambió y hay una nueva solicitud en el Kiosko.

Los PDFs generados se guardan en:

```
files/fichas/
└── <codigo>_<fecha>_<hora>.pdf
```

---

## 🌐 Sincronización en red (P2P)

AstroLine no requiere servidor central. Cada equipo descubre a los demás en la LAN automáticamente al arrancar y mantiene los datos sincronizados.

```
Equipo A (Admin)          Equipo B (Kiosko)         Equipo C (Funcionario)
      │                         │                         │
      │── UDP broadcast ────────►│◄──────────────────── ──│
      │                         │                         │
      │◄─────── HTTP POST (empresa.json) ─────────────────│
      │                         │                         │
      │── HTTP POST (fichas.json) ──────────────────────► │
      │                         │                         │
      └─── polling cada 15s ────┘─────────────────────────┘
```

- Puerto UDP `9090` — descubrimiento de peers
- Puerto TCP `8080` — transferencia de archivos JSON e imágenes
- `configuracion.json` está excluido del P2P (es por equipo)

---

## 📅 Estado del proyecto

El proyecto está **completado** ✅. Todos los módulos están implementados y funcionales.

| Módulo | Estado |
|---|---|
| Arquitectura base (MVC + FlowController + GsonUtil) | ✅ Completado |
| Modelos de datos y DTOs | ✅ Completado |
| Módulo Administrador (empresa, funcionarios, trámites, sucursales, estadísticas) | ✅ Completado |
| Módulo Kiosko (teclado numérico, PDF, preferencial por PIN) | ✅ Completado |
| Módulo Funcionario (llamado de fichas, foto cliente, selección manual) | ✅ Completado |
| Módulo Proyección (marquee, reloj, audio TTS, historial) | ✅ Completado |
| Sistema P2P (UDP discovery + HTTP sync + merge por lastModified) | ✅ Completado |
| Generación de PDF con PDFBox (diseño cosmos) | ✅ Completado |
| Registro de clientes con cámara web | ✅ Completado |
| Pantalla de estadísticas con gráficas | ✅ Completado |
| Configuración local por equipo | ✅ Completado |

---

<div align="center">

*Desarrollado con ☕ en Costa Rica*  
*Universidad Nacional · Sede Brunca · Campus Pérez Zeledón · 2026*

</div>
