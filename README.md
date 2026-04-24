<div align="center">

<img src="src/main/resources/cr/ac/una/astroline/resource/logo.png" alt="AstroLine Logo" width="120"/>

# AstroLine

**Sistema de gestión y control de fichas de atención**

[![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25.0.2-blue?style=flat-square)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.8-red?style=flat-square&logo=apachemaven)](https://maven.apache.org/)
[![MaterialFX](https://img.shields.io/badge/MaterialFX-11.16.1-purple?style=flat-square)](https://github.com/palexdev/MaterialFX)
[![AtlantaFX](https://img.shields.io/badge/AtlantaFX-2.1.0-teal?style=flat-square)](https://github.com/mkpaz/atlantafx)
[![License](https://img.shields.io/badge/Licencia-Académica-green?style=flat-square)]()

*Tarea Programada — Programación II · Universidad Nacional de Costa Rica*  
*Sede Regional Brunca, Campus Pérez Zeledón · 2026*

</div>

---

## 📋 Descripción

AstroLine es una aplicación de escritorio desarrollada en JavaFX que permite la **asignación y control de fichas de atención** para empresas con múltiples sucursales y estaciones de servicio. El sistema cuenta con 4 módulos de acceso diferenciados según el rol del usuario, con soporte para sincronización en tiempo real entre equipos en una misma red local.

---

## 🎭 Módulos del sistema

| Módulo | Descripción |
|---|---|
| 🛡️ **Administrador** | Gestión de parámetros, trámites, sucursales, estaciones, clientes e indicadores |
| 🖥️ **Kiosko** | Asignación de fichas táctil con generación de PDF y atención preferencial |
| 👔 **Funcionario** | Gestión de llamados de fichas por estación en tiempo real |
| 📺 **Proyección** | Pantalla pública con historial de fichas, audio de llamado y avisos en scroll |

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
| SceneBuilder | — | Diseño de vistas FXML |
| NetBeans | — | Entorno de desarrollo |

---

## 🏗️ Arquitectura

El proyecto sigue el patrón **MVC (Model-View-Controller)**:

```
src/
└── main/
    ├── java/cr/ac/una/astroline/
    │   ├── controller/     ← Controllers de cada vista
    │   ├── model/          ← Clases de datos (Ficha, Cliente, Tramite...)
    │   ├── service/        ← Lógica de negocio (singletons)
    │   └── util/           ← FlowController, GsonUtil, PathManager, AppContext...
    └── resources/cr/ac/una/astroline/
        ├── view/           ← Archivos FXML
        ├── resource/       ← Imágenes, audio, íconos
        └── styles/         ← Hojas de estilo CSS
```

### Convenciones arquitectónicas

- Todos los controllers extienden la clase base `Controller`.
- `initialize(URL, ResourceBundle)` solo realiza configuración estática (bindings, listeners).
- El hook de FlowController es `initialize()` sin argumentos, donde se lee `AppContext`.
- Las claves de `AppContext` siguen el patrón `"entidadParaEditar"` (entrada) y `"ultimaEntidadId"` (salida).
- Los servicios son singletons accesibles únicamente vía `getInstancia()`.
- Sin estilos inline; toda la presentación va en clases CSS.

---

## 📁 Gestión de archivos y rutas

El sistema maneja dos ubicaciones distintas para sus archivos, resolviendo el problema de múltiples módulos ejecutándose en la misma máquina:

### Bootstrap (único por equipo)

```
Windows  →  C:\Users\<usuario>\.astroline\properties.json
macOS    →  /Users/<usuario>/.astroline/properties.json
Linux    →  /home/<usuario>/.astroline/properties.json
```

Este archivo se crea automáticamente en el primer arranque y define la ruta donde viven los datos compartidos. Al ser una ruta absoluta y fija, todos los módulos (Admin, Kiosko, Funcionario, Proyección) convergen al mismo archivo sin importar desde qué carpeta se ejecuten.

### Datos compartidos

Por defecto en `~/Documents/AstroLine/`. Puede cambiarse desde el módulo Admin para apuntar a una carpeta de red.

```
AstroLine/
├── configuracion.json   — sucursal y estación asignadas al equipo
├── empresa.json         — datos y logo de la empresa
├── sucursales.json      — sucursales y estaciones registradas
├── tramites.json        — tipos de trámite disponibles
├── clientes.json        — registro de clientes
├── funcionarios.json    — cuentas de funcionarios
├── fichas.json          — fichas activas del día
├── historial.json       — fichas archivadas
├── fotos/               — fotografías de clientes
└── logoEmpresa/         — logo cargado desde Admin
```

---

## 🚀 Ejecución

### Desde el IDE (desarrollo)

```bash
# Clonar el repositorio
git clone https://github.com/JohanDanilo/AstroLine.git
cd AstroLine

# Ejecutar desde Maven (Vista de Eleccion de modo por defecto)
mvn javafx:run
```

### Desde JAR (distribución)

Cada módulo cuenta con un launcher `.lnk` (Windows):

```
Astroline-admin.lnk
Astroline-kiosko.lnk
Astroline-funcionario.lnk
Astroline-proyeccion.lnk
```

En Windows, ejecute `setup.bat` con el Archivo AstroLine-1.0-SNAPSHOT.jar al lado, dentro de la carpeta del módulo para generar los accesos directos `.lnk` adaptados a la ruta del JDK instalado en ese equipo. No borre el archivo AstroLine-1.0-SNAPSHOT.jar ya que cada lnk lo busca la misma carpeta en la que se encuentre.

---

## 🔐 Credenciales por defecto

| Dato | Valor |
|---|---|
| Usuario Admin | `admin` |
| Contraseña | `1234` |
| PIN de Kiosko | `1234` |

---

## 👥 Equipo de desarrollo

| Desarrollador | Módulos |
|---|---|
| **Johan Danilo** (`@JohanDanilo`) | Líder · Kiosko · Proyección · Admin ·Infraestructura |
| **Jessica** (`@JekaCordero`) | Funcionario · Proyección |

---

## 📅 Estado del proyecto

| Issue | Descripción | Estado |
|---|---|---|
| #1 | Arquitectura base y estructura MVC | ✅ Completado |
| #2 | Modelos de datos y persistencia JSON | ✅ Completado |
| #3 | Módulo Kiosko con generación de PDF | ✅ Completado |
| #4 | Módulo Funcionario | ✅ Completado |
| #5 | Módulo Administrador | ✅ Completado |
| #6 | Módulo Proyección | ✅ Completado |
| #7 | Gestión de rutas multi-módulo (`PathManager`) | ✅ Completado |

---

<div align="center">

*Desarrollado con ☕ en Costa Rica*  
*Universidad Nacional · Sede Brunca · 2026*

</div>
