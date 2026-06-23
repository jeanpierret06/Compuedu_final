# 🎓 Sistema de Gestión Académica (PortalEdu)

Plataforma web integral desarrollada con **Spring Boot** para la gestión de convocatorias académicas, becas y procesos de inscripción. El sistema conecta a Estudiantes, Instituciones y Administradores en un entorno seguro y eficiente.

---

## 🚀 Características Principales

### 👨‍💻 Administrador
* **Gestión de Usuarios:** Listar, buscar, filtrar y eliminar usuarios.
* **Moderación:** Anular o reactivar convocatorias publicadas por instituciones.
* **Reportes:** Exportar listado de usuarios a **PDF**.
* **Dashboard:** KPIs en tiempo real (Total usuarios, convocatorias activas, etc.).

### 🏫 Institución (Aliado)
* **Publicación:** Crear nuevas convocatorias de becas o cursos.
* **Gestión:** Ver listado de sus convocatorias publicadas y su estado.
* **Seguimiento:** Ver tabla de estudiantes inscritos a sus ofertas.

### 🎓 Estudiante
* **Registro Público:** Creación de cuenta autónoma.
* **Postulación:** Ver ofertas disponibles e inscribirse con un clic.
* **Mis Trámites:** Historial de inscripciones.
* **Comprobantes:** Descarga automática de constancia de inscripción en **PDF**.
* **Perfil:** Edición de datos de contacto.

---

## 🛠️ Tecnologías Utilizadas

* **Backend:** Java 17+, Spring Boot 3.x (Spring Security, Spring Data JPA, Spring Web).
* **Frontend:** Thymeleaf, Bootstrap 5, FontAwesome, Google Fonts (Poppins).
* **Base de Datos:** MySQL.
* **Reportes:** OpenPDF (iText fork).
* **Control de Versiones:** Git / GitHub.

---

## ⚙️ Instalación y Configuración

### 1. Clonar el repositorio
```bash
git clone [https://github.com/TU_USUARIO/TU_REPOSITORIO.git](https://github.com/TU_USUARIO/TU_REPOSITORIO.git)
cd AccesoUsuarios
