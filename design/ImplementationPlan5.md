# Plan de Implementación de la Arquitectura - La Toscana

Este documento detalla el plan de implementación estructurado en iteraciones pequeñas y sub-etapas. Sigue la estrategia de priorizar un **Producto Mínimo Viable (MVP) sólido** que ataque inmediatamente los problemas de mayores pérdidas operativas (fugas en caja y órdenes manuales). Además, alinea la estructura de desarrollo documentada en las Fases del diseño arquitectónico, especificando las tecnologías, herramientas y enfoques de infraestructura adecuados.

---

## Iteración 1: Esqueleto del Sistema (Infraestructura Core)
**Objetivo de la iteración:** Establecer la infraestructura base, aislar el contenedor principal (Branch Node), preparar la base de datos modular local y orquestar las plantillas base del frontend, preparando el terreno sólido para los módulos de negocio.  
**Número de tareas específicas:** 4  
**Entregables (Tecnologías y Herramientas a implementar):**
- **SGBD:** *PostgreSQL* (Instalación local) administrado mediante *pgAdmin* o *DBeaver*. Control de versiones de la BD automatizado empleando herramientas de migración como *Flyway* o *Liquibase*.
- **Backend (Monolito Modular):** Proyecto base construido en *Java 21 con Spring Boot* (recomendado por su soporte robusto a modularidad y ecosistema empresarial) o alternativamente *Node.js con NestJS*. IDE recomendado: *IntelliJ IDEA* o *VS Code*.
- **Frontend (MPA Local):** Plantillas base responsivas estáticas/dinámicas usando el motor *Thymeleaf* (para Spring) con *Bootstrap 5* o *TailwindCSS* mediante CDN local para un maquetado rápido e independiente.
- **Herramienta de Testing:** *Postman* o *Insomnia* para validación del endpoint `/api/health`.

### Sub-etapas:
1. **Etapa 1.1: Setup del Branch Application Server.**
   - Inicialización del proyecto Backend (Monolito Modular).
   - Estructuración de directorios y paquetes lógicos por "Bounded Context" (`Identity`, `Orders`, `Catalog`, `Financial`, `Inventory`, `Sync`).
   - Configuración de la Inversión de Control (IoC) y garantizar aislamiento entre módulos.
2. **Etapa 1.2: Modelado Base de Datos Local (PostgreSQL).**
   - Creación de esquemas independientes por módulo.
   - Elaboración de scripts de inicialización base SQL.
3. **Etapa 1.3: Setup del Frontend (MPA).**
   - Configuración del motor de plantillas para la aplicación web contenida en el mismo servidor.
   - Creación del *layout* base general incluyente la cascada de navegación y responsividad (Desktop/Tablet).
4. **Etapa 1.4: Monitoreo de Conectividad.**
   - Implementación del controlador `ConnectivityService`.
   - Creación del indicador visual global de conectividad (rojo/verde) en el Header de toda la UI del personal.

---

## Iteración 2: Flujo Principal de Ventas (Órdenes y Pagos)
**Objetivo de la iteración:** Completar el despliegue del punto de venta funcional (MVP operativo) de principio a fin, abordando la urgencia del negocio: eliminar órdenes canceladas sin trazabilidad y cobros erróneos por sumar manualmente.  
**Número de tareas específicas:** 5  
**Entregables (Tecnologías y Herramientas a implementar):**
- **Capa de Persistencia:** Mapeo Objeto-Relacional (ORM) usando *Spring Data JPA / Hibernate* garantizando el Optimistic Locking (Anotación `@Version` para evitar colisiones en transacciones concurrentes).
- **Control de Interfaces:** Formularios ágiles optimizados para eventos *touch* (usando *Vanilla JS* simple para interacciones DOM directas que no requieran SPA competa).
- **Generación de Archivos:** Librería de creación de PDFs como *JasperReports* o *iText* para emitir el "Ticket/Recibo" virtual asociado.

### Sub-etapas:
1. **Etapa 2.1: Módulo de Catálogo (Seed & Read-Only).**
   - Inserción de un catálogo "Semilla" (`seeds.sql`) en `catalog.products` y `catalog.categories`.
   - Implementación de `CatalogPublicAPI` (listado y buscador).
2. **Etapa 2.2: Gestión de Órdenes (Backend Core).**
   - Implementación del `OrderApplicationService` (funciones: *createOrder*, *addLine*, *removeLine*).
   - Restricción del estado de la Orden (Máquina de estados OPEN -> CANCELLED/CLOSED) y concurrencia optimista aplicable por transacción.
3. **Etapa 2.3: Interfaz de Mesero.**
   - Interfaz (UI adaptada para Tablet) permitiendo alta manual de líneas, visualizando precios con protección *offline*.
   - Acoplamiento a endpoints del Controlador `WaiterPOSController`.
4. **Etapa 2.4: Operaciones Financieras (Cashier Core).**
   - Implementación de lógica `SaleApplicationService` expuesta mediante método de caja `closeSale`.
   - Declaración de transacción unitaria de base de datos compartida compartiendo `Connection` DB (`@Transactional(propagation = REQUIRED)`) para anclar la Orden cerrándose al Ticket financiero.
5. **Etapa 2.5: Interfaz de Cajero.**
   - Vista para obtener la pantalla de total cobrable consultando un endpoint.
   - Emulación final de pago (Efectivo/Tarjeta) renderizando pantalla y formato PDF de Recibo.

---

## Iteración 3: Seguridad Multirrol y Tolerancia a Fallas (Offline First)
**Objetivo de la iteración:** Blindar financieramente la arquitectura, trazando qué empleado generó qué acción, y afianzar la principal restricción técnica: garantizar retención de datos en caídas prolongadas de conexión.  
**Número de tareas específicas:** 5  
**Entregables (Tecnologías y Herramientas a implementar):**
- **Soporte de Autenticación Offline:** Implementación de *Spring Security* modificado para validar peticiones sin ir a OAUTH central. Uso de la librería jjwt para firma y validación de tokens *JWT*. Hash de contraseñas usando el algoritmo intrínseco de *BCrypt*.
- **Planificador Local:** Uso de `ScheduledExecutorService` o anotaciones directas `@Scheduled` para arrancar el cron de `SyncWorker`.
- **Nube Inicial:** Provisionamiento de una máquina virtual o contenedor (*Docker Compose*) en la nube (ej. *AWS EC2*, *Google Cloud Compute*, o *DigitalOcean Droplet*) para hospedar el API receptora y la *AWS RDS PostgreSQL* como Central DB.

### Sub-etapas:
1. **Etapa 3.1: Identidad y Acceso (Backend).**
   - Implementación de la matriz RBAC tras la `IdentityPublicAPI` y persistir sesiones temporalmente en memoria o DB interna.
   - Integración de Bcrypt para hasheo seguro y criptográfico.
2. **Etapa 3.2: Middleware de Autenticación y Autorización.**
   - Elaboración del `RoleBasedAccessControlFilter` validando jerarquías (Cajero/Gerente/Mesero) por cada request HTTP al Node local.
   - Vista de `Login` nativo implementada en el Layout.
3. **Etapa 3.3: Capa de Auditoría Transversal.**
   - Programación Operada a Aspectos (AOP, e.g. `@Aspect` en Spring) insertada al nivel *Repository* para generar "Logs de Auditoría" en la tabla *audit_log*.
4. **Etapa 3.4: Base de Sincronización (Outbox Pattern).**
   - Adición del enganche post-transacción que re-envíe todas las entidades exitosas dentro de la BD a la tabla puente `sync.sync_queue`.
5. **Etapa 3.5: Cloud Sync Hub y Worker (Prueba de Concepto).**
   - Setup básico asilado en la Nube del contenedor receptor: *Sync API* REST y Central DB.
   - Refinamiento colocalizado del *Sync Worker* en el Branch (ejecución periódica leyendo del schema `sync` y lanzando peticiones POST cifradas HTTPS hacia la Nube).

---

## Iteración 4: Control de Inventario y Arqueo de Caja
**Objetivo de la iteración:** Mitigar el segundo mayor problema financiero de la empresa. Habilitar el módulo capaz de descontar recetas automáticamente de manera precisa vinculando el cierre del corte financiero local del Gerente.  
**Número de tareas específicas:** 4  
**Entregables (Tecnologías y Herramientas a implementar):**
- **Arquitectura Basada en Eventos (EDA Local):** Utilización de *Spring ApplicationEvents* en arquitectura tipo Pub/Sub local síncrona, desacoplando la transacción del flujo financiero del flujo de almacén.
- **Backend UI:** Vistas bloqueadas exclusivamente para acceso `MANAGER`, construidas en `Thymeleaf` / `EJS` pero con notificaciones flotantes con *Toastify JS* o *SweetAlert*.

### Sub-etapas:
1. **Etapa 4.1: Modelos de Inventario.**
   - Tablas definitivas para `Ingredient`, stock total, recetas y definiciones en API base de inventario.
2. **Etapa 4.2: Integración de Venta con Inventario.**
   - Configuración de eventos de dominio (`SaleClosedEvent`). Un *listener* en el Módulo Inventario capturará el evento y deducirá los gramos correspondientes a las líneas facturadas.
3. **Etapa 4.3: Arqueo de Caja Automático.**
   - Funciones SQL de Agrupación (GROUP BY y SUM) expuestas por API validando las órdenes generadas entre la marca de tiempo de apertura y cierre de la sucursal.
4. **Etapa 4.4: Gestión Administrativa Local.**
   - Vistas web para operaciones de reajuste forzado de inventario para el gerente por merma o error.
   - Lógica que empuja las alarmas de inventario a las alertas UI del usuario Manager.

---

## Iteración 5: Reportes Consolidados, Cocina y Compras
**Objetivo de la iteración:** Digitalizar toda la operatividad en trastienda y proporcionar un observatorio analítico único centralizado en el *Cloud Sync Hub* destinado al gobierno central (Dueño/Contador).  
**Número de tareas específicas:** 4  
**Entregables (Tecnologías y Herramientas a implementar):**
- **Flujo de Tiempo Real en Cocina:** Manejo de asincronismo en el frontend local ya sea mediante técnica *Short Polling Inteligente* en JS o levantando una conexión *Server-Sent Events (SSE)* / *WebSockets*.
- **Dashboard Cloud Reporting (Dueño):** Desarrollo de una *Single Page Application* administrativa separada. Framework: *React.js* o *Vue.js* (por su rico ecosistema de librerías visuales como *Recharts* o *Chart.js* para métricas analíticas).
- **Procesamiento Analítico Cloud:** Consultas nativas paginadas, expuestas mediante JSON REST API estables desplegadas usando un *Application Load Balancer*.

### Sub-etapas:
1. **Etapa 5.1: Flujo de Cocina/Barra.**
   - Creación del display de interfaz "Cocina" (vista modo de Kiosko HTML, optimizado para TV smart local) mostrando tableros en estados `SENT_TO_KITCHEN` y `READY`.
   - Modificación al state management de Notificaciones hacia interfaz Meseros (Alistando plato en barra).
2. **Etapa 5.2: Cloud Sync Hub (Reporting API).**
   - Endpoints de extracción analítica de data masiva consolidada en el Server Cloud Central interactuando con metadatos de las 5 sucursales simultáneas.
   - Paneles o Dashboards analíticos UI construidos para uso directo del inversor/dueño.
3. **Etapa 5.3: Catalogamiento Central.**
   - Exponer un CMS ligero UI en el Cloud Sync Hub, permitiendo al dueño actualizar descripciones o precios globalmente. El Hub generará un payload que las sucursales sincronizarán de bajada inversamente (Top-Down sync configurado).
4. **Etapa 5.4: Módulo de Compras.**
   - Panel de Interfaz de Gerente para enlistar proveedores y administrar `PurchaseOrders`.
   - Flujo de Entrada de Mercancía vinculando directamente al modelo de `StockAdjustment`.

---

## Iteración 6: Valor Agregado (Digitalización al Cliente)
**Objetivo de la iteración:** Finalizada la contención de mermas y eficiencias productivas, enfocarse en la mejora comercial creando nuevos vehículos para atraer clientes recurrentes, incrementando las utilidades.  
**Número de tareas específicas:** 3  
**Entregables (Tecnologías y Herramientas a implementar):**
- **Menú Digital (App Comensal Web):** Diseño de un PWA responsivo puro (usando un framework ligero como *Preact* o *Astro* + *Tailwind CSS* para reducir tiempo de pintar primer frame / FCP), diseñado para ejecutarse localmente vía código QR.
- **Motor de Reglas Flexible:** Para promociones complejas, integración ligera utilizando patrones *Strategy* de interfaces nativas o una librería de validación de heurísticas como *EasyRules* (Java).

### Sub-etapas:
1. **Etapa 6.1: Menú Digital Web (QR).**
   - Programación de la *Single Page Application* pública orientada a Mobile-First, la cual consumirá por red local LAN las APIS read-only del `Catalog API` y devolverá vista optima de platillos.
2. **Etapa 6.2: Motor de Promociones.**
   - Modelado de entidad `Promotion`. Despliegue de interceptores de descuento aplicados por campaña activa directo al subtotal temporal calculado por la orden (`SaleApplicationService`).
3. **Etapa 6.3: Registro de Clientes.**
   - Agregar modal en la pantalla de Cajero para tomar los datos clave del ticket al cierre rápido de venta, apuntados en BD y enviados al sistema Hub como base de fidelización.
