# Resumen de la Iteración 1: Esqueleto del Sistema (Infraestructura Core)

Este documento resume de forma estructurada los avances, componentes creados y decisiones técnicas aplicadas durante el setup inicial del sistema La Toscana (Branch Node).

## 1. Tecnologías Implementadas

| Tecnología | Rol en el Proyecto | Justificación |
|---|---|---|
| **Java 21** | Lenguaje Base | LTS más reciente, rendimiento optimizado y pilar del ecosistema modular corporativo moderno. |
| **Spring Boot 3.2.x** | Framework Backend | Provee auto-configuración, servidor embebido (Tomcat) e Inyección de Dependencias, fungiendo como el contenedor del *Modular Monolith*. |
| **PostgreSQL** | SGBD Relacional | Motor transaccional fiable y gratuito que permite separación estricta de dominios mediante Esquemas (*Schemas*). |
| **Flyway** | Migraciones de BD | Herramienta de control de versiones SQL que garantiza que la estructura de la base de datos (*esquemas base*) se cree automáticamente al iniciar el servidor. |
| **Thymeleaf** | Motor de Plantillas (SSR) | Permite renderizar "Multi-Page Applications" (MPA) y despachar la UI desde el mismo Branch Node sin compilar proyectos de frontend por separado. |
| **Bootstrap 5 (CDN)** | Framework CSS / UI | Permite el enmaquetado rápido y responsivo (*touch-friendly*) necesario para las pantallas de las tablets y TVs del restaurante. |
| **Vanilla JS (Fetch API)** | Reactividad Básica Frontend | Implementado para el *Long-Polling* de la red local, evitando recargar la página para consultar el status operativo. |
| **Spring Security** | Framework de Seguridad | Implementado en el `pom.xml` como cimiento temprano; en esta etapa, ajustado para no generar fricciones durante nuestro MVP de negocio. |

## 2. Clases y Componentes Creados

| Archivo / Componente | Ubicación Lógica | Propósito y Responsabilidad |
|---|---|---|
| `pom.xml` | Raíz del proyecto | Administra paquetes y dependencias base (Web, Data JPA, Postgres, Flyway, Thymeleaf, Security y JWT). |
| `application.yml` | `src/main/resources/` | Registra y centraliza la configuración: propiedades de acceso DB (`localhost:5432`), puerto y Dialectos de Hibernate. |
| `V1__init_schemas.sql` | `db/migration/` | Secuencia SQL fundacional. Crea los 6 espacios de BD (`identity`, `orders`, `catalog`, `financial`, `inventory`, `sync`). |
| `BranchNodeApplication.java` | `com.latoscana.branchnode` | Clase `main` que arranca todo el nodo como proceso de Spring Boot. |
| `ConnectivityController.java` | `common/presentation/` | API de lectura rápida (`/api/health`) que simulará al SyncWorker notificando si la sucursal está "Online al Hub" o "Fuera de línea". |
| `HomeController.java` | `common/presentation/` | Controlador encargado de renderizar visualmente el índice principal del cajero. |
| `SecurityConfig.java` | `common/` | Componente vital temporal. Desactiva el cerrojo "por defecto" de Spring Security y deshabilita su pantalla base para centrarnos en el MVP de ventas. |
| `layout.html` | `templates/` | *Fragmento* Thymeleaf reutilizable. Contiene el esqueleto HTML, NavBar, el globo de conectividad y CDN's. |
| `index.html` | `templates/` | Panel del dashboard blanco donde descansan las tarjetas directrices. Hereda del `layout.html`. |
| `connectivity.js` | `static/js/` | Lógica asíncrona. Pide un status al servidor cada 15s y enciende el indicador verde o rojo de conectividad. |
| `.gitignore` | Raíz del proyecto | Asegura que el repositorio Git local no acumule las pesadas salidas de la carpeta `target/` ni basuras temporales. |

## 3. Problemáticas y Situaciones Atendidas

| Situación Ocurrida | Solución y Enfoque Arquitectónico |
|---|---|
| **Estructuración de Dominios (Monolito Modular)** | Aunque se inicializó un solo proyecto Java, se sentaron las bases para aislamiento físico y semántico. La DB se configuró para usar Esquemas divididos en lugar de tablas aglomeradas, obligando a los futuros módulos a comunicarse por APIS públicas. |
| **Garantizar la visibilidad del estado Offline** | El requerimiento exigía que la plantilla supiera su estatus sin afectar la operación; se resolvió creando el mecanismo *Ping/Echo* asíncrono implementado puramente con un endpoint que la web interroga en bucle temporal. |
| **Bloqueo autogenerado (Spring Security Lockout)** | Al preparar las dependencias para la Iteración 3 de inicio, Spring impuso un control de acceso aleatorio bloqueando el desarrollo. Aplicamos una clase de configuración (Anotaciones `@Bean` y `@EnableWebSecurity`) omitiendo este cerrojo para poder circular libremente en la Iteración 2. |
| **Preparación para Retroceder (Rollbacks Locales)** | Para proteger nuestra primera iteración de errores catastróficos futuros, se configuró con éxito un `.gitignore` y se introdujo la cultura de commits, en vez de depender de guardar copias empacadas (ZIPs) del proyecto. |
