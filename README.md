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
*Sede Regional Brunca, Campus Pérez Zeledón*

</div>

---

## 📋 Descripción

AstroLine es una aplicación de escritorio desarrollada en JavaFX que permite la **asignación y control de fichas de atención** para empresas con múltiples sucursales y estaciones de servicio. El sistema cuenta con 4 módulos de acceso diferenciados según el rol del usuario.

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

- **Java 25** — lenguaje principal
- **JavaFX 25.0.2** — interfaz gráfica
- **Maven** — gestión de dependencias y build
- **Gson 2.10.1** — persistencia de datos en JSON
- **MaterialFX 11.16.1** — componentes UI modernos
- **AtlantaFX 2.1.0** — sistema de temas visuales
- **SceneBuilder** — diseño de vistas FXML
- **NetBeans** — entorno de desarrollo

---

## 🏗️ Arquitectura

El proyecto sigue el patrón **MVC (Model-View-Controller)**:

```
src/
└── main/
    ├── java/cr/ac/una/astroline/
    │   ├── controller/     ← Controllers de cada vista
    │   ├── model/          ← Clases de datos (Ficha, Cliente, Tramite...)
    │   ├── service/        ← Lógica de negocio
    │   └── util/           ← FlowController, GsonUtil, AppContext...
    └── resources/cr/ac/una/astroline/
        ├── view/           ← Archivos FXML
        ├── resource/       ← Imágenes, audio, íconos
        └── styles/         ← Hojas de estilo CSS
```

Toda la información del sistema se persiste en archivos **JSON** dentro de la carpeta `data/` en el directorio raíz del proyecto.

---

## 👥 Equipo de desarrollo

| Desarrollador | Módulo |
|---|---|
| **Johan Danilo** | Líder · Kiosko · Proyección |
| **José** | Administrador |
| **Jessica** | Funcionario |

---

## 📦 Requisitos

- JDK 25
- Apache NetBeans (recomendado)
- Maven 3.8+
- SceneBuilder (para edición de vistas FXML)

---

## 🚀 Ejecución

```bash
# Clonar el repositorio
git clone https://github.com/JohanDanilo/AstroLine.git

# Entrar al directorio
cd AstroLine

# Ejecutar el proyecto
mvn javafx:run
```

> **Nota:** El proyecto puede ejecutarse con `mvn javafx:run` desde el panel de Maven en NetBeans o desde la terminal. También se puede usar el botón Run de NetBeans o el IDE de su preferencia directamente.

---

## 📁 Datos del sistema

Al ejecutarse por primera vez, el sistema crea automáticamente la carpeta `data/` con los archivos JSON necesarios:

```
data/
├── empresa.json
├── tramites.json
├── sucursales.json
├── clientes.json
└── fichas.json
```

---

## 📅 Estado del proyecto

| Issue | Descripción | Estado |
|---|---|---|
| #1 | Arquitectura base del profesor adaptada | ✅ Completado |
| #2 | Pull Request — arquitectura base | ✅ Mergeado |
| #3 | Modelos de datos base y GsonUtil | 🔄 En progreso |
| #4 | Navegación principal y selección de rol | ⏳ Pendiente |
| #5 | CSS base del sistema | ⏳ Pendiente |

---

<div align="center">

*Desarrollado con ☕ en Costa Rica*
*Universidad Nacional · Sede Brunca · 2026*

</div>