# Resumen de Iteración 3: Seguridad, Sincronización Offline y Estabilización

## 1. Descripción de la Iteración
La **Iteración 3** de la arquitectura de *La Toscana* se centró en sentar las bases de seguridad, controles de acceso y operaciones en modo desconectado de los Nodos de Sucursal (Branch Nodes). El objetivo principal fue inyectar un robusto sistema de Autenticación mediante **JWT (JSON Web Tokens)** gestionados vía *HTTP-Only Cookies*, implementar el diseño de **Control de Acceso Basado en Roles (RBAC)** y desplegar la infraestructura base del **Patrón Outbox** (mediante cola de sincronización) para la eventual resiliencia y subida de datos a la nube. También se refino la Interfaz de Usuario para ser 100% resistente a caídas de Internet (Intranet-Ready).

---

## 2. Mapa de Cambios Estructurales

### 🟢 Componentes Creados
| Componente / Archivo | Capa / Módulo | Propósito |
| :--- | :--- | :--- |
| `V4__...sql`, `V5__...sql` | Migraciones Flyway | Definición y corrección (`BIGINT`) de tablas `users`, `audit_log`, y `sync_queue`. |
| `JwtTokenProvider` | Infraestructura / Identidad | Generación y validación de tokens seguros y su caducidad cronológica. |
| `JwtAuthenticationFilter` | Infraestructura / Identidad | Filtro perimetral del ciclo de vida web que extrae cookies e inyecta la identidad al contexto. |
| `RoleBasedLoginSuccessHandler`| Infraestructura / Identidad | Enrutador automático de flujos post-login (`/manager`, `/cashier`, `/waiter`). |
| `SyncQueue`, `SyncWorker` | Infraestructura / Sincro | Modelado del esquema Outbox para registrar eventos offline para envío asíncrono. |
| `403.html` | Presentación / Error | Interfaz estética y amigable que notifica sobre restricciones de Acceso Denegado (RBAC). |
| Recursos Estáticos Locales | `static/css/`, `static/js/` | Incorporación nativa de Bootstrap y Tailwind para erradicar la dependencia de Internet (CDN). |

### 🟡 Componentes Refinados (Actualizados)
| Componente / Archivo | Capa / Módulo | Cambio Realizado |
| :--- | :--- | :--- |
| `pom.xml` | Configuración Global | Inserción de la dependencia `spring-boot-starter-aop` requerida para interceptores transaccionales. |
| `SecurityConfig` (nuevo) | Infraestructura / Identidad | Declaración de políticas de sesión State-less (`SessionCreationPolicy.STATELESS`), filtros JWT y reglas de autorización HTTP de rutas según Roles. |
| `*Controller.java` | Presentación | Inyección del objeto `Authentication` a `HomeController`, `CashierPOSController` y Waiter para habilitar el contexto visual en `Thymeleaf`. |
| `layout.html`, `*pos.html` | Presentación / Thymeleaf | Integración de `<form>` de Cerrar Sesión, eliminación de CDNs e inyección dinámica del nombre de usuario y rol actual en las barras de tareas. |
| `SyncQueue` (Entidad) | Dominio / Sincro | Se incluyó la vital directiva `@JdbcTypeCode(SqlTypes.JSON)` al atributo `payload` para empatar con `JSONB` de PostgreSQL. |
| Interfaces POS JS | Interacción Web | Lógica de recargas para evitar el "ghosting", el congelamiento de botones post-pdf, y actualización clara al flujo de trabajo del mesero. |

### 🔴 Componentes Eliminados
| Componente / Archivo | Capa / Módulo | Motivo de Eliminación |
| :--- | :--- | :--- |
| `common.SecurityConfig` | Configuración Legacy | Archivo obsoleto de la Iteración 2 que bloqueaba y colisionaba con la nueva y más robusta inyección JWT. |
| Mocks Temporales / Rutas colisión | Presentación / Identity | Limpieza del controlador `LoginUIController` eliminando su toma hostil de la raíz `/` y los renders inútiles de prueba de roles que estorbaban a los POS reales. |

---

## 3. Resolución de Problemas (Troubleshooting Log)

| 🐛 Problema Reportado | 🔍 Análisis Arquitectónico | 🛠️ Solución Aplicada |
| :--- | :--- | :--- |
| **Colisión de Beans: `WebSecurityConfiguration` fallaba en arranque.** | Múltiples definiciones superpuestas del `@EnableWebSecurity` en el proyecto ahogaban el Contexto de Spring Boot. | Destrucción de la clase legado de seguridad en la capa Common, unificando todo el control en el nuevo `SecurityConfig` del módulo Identity. |
| **Ambigüedad de Rutas Web (`/` mapping collision).** | Tanto el `HomeController` como el `LoginUIController` competían violentamente por inyectar el `@GetMapping("/")`. | Eliminación del método `rootRedirect` dentro de `LoginUIController`. El index original se respetó y se configuraron redirigidores dedicados en SecurityConfig. |
| **Validación de Esquema Hibernate bloqueada (`int` vs `bigint`).** | Spring definía los IDs como `Long`, pero PostgreSQL creó las secuencias nativas como `SERIAL` (int de 32 bits). | Codificación de un parche Flyway V5 usando sintaxis estructural: `ALTER TABLE ... ALTER COLUMN id TYPE BIGINT`. |
| **Excepción `PasswordEncoder` sin mapear al insertar SQL manual**. | Las contraseñas en Spring Configuración 6+ sin prefijo desencadenan pánico criptográfico (`id "null"`). | Instruir la inserción de hashes encriptados mediante el prefijo estructural `{bcrypt}$2a$10...`. |
| **Fallo en persistencia Outbox (Error `jsonb` vs `character varying`).** | El motor JPA al ver un String natural de Java intentaba inyectar un VARCHAR al campo complejo `jsonb` de Postgres. | Configuración explícita de anotaciones `@JdbcTypeCode(SqlTypes.JSON)` orientando a Hibernate al uso correcto de dialectos Postgres. |
| **Órdenes "Fantasma" saturando la caja de cobro.** | Javascript creaba una orden vacía contra el Backend de forma voraz cada vez que el Mesero entraba o recargaba la página. | Refactorización de comportamiento local (Lazy Loading): La orden solo nace en Base de Datos cuando ocurre el clic inicial sobre un platillo real del Catálogo. |
| **Congelamiento de UX al generar el PDF del ticket en Caja.**| Al abrir el Blob Binario, el hilo nativo detiene la invocación del bloque JS `finally`, dejando el botón trabado en "Procesando...". | Enrutamiento del ticket a la etiqueta auxiliar `target='_blank'`, forzando el reseteo visual agresivo del DOM inmediatamente antes del trigger nativo de descarga en el navegador. |
| **SPOF (Punto único de fallo) ante caídas extremas del ISP.** | Al perder el Internet externo, dependencias estéticas (Bootstrap y Tailwind) desde CDN (`jsdelivr.net`) no cargaban. | Secuestro y empaquetamiento directo en Tomcat local de los CSS, JS, binarios de WebFonts y Scripts Minificados. Modificación total de punteros `th:href` relativos, sellando la capa de Presentación en modo 100% Offline. |
