# Resumen de Iteración 2: Flujo Principal de Ventas

A continuación se detalla el resumen técnico de los componentes agregados y las situaciones resueltas durante el desarrollo de la Iteración 2 del sistema La Toscana.

## 1. Tecnologías Incorporadas

| Tecnología / Librería | Propósito / Uso en la Iteración |
|-----------------------|---------------------------------|
| **Spring Data JPA**   | Capa de persistencia, Mapeo Objeto-Relacional (ORM) y gestión de Lock Optimista (`@Version`) para transacciones en la red. |
| **iText (5.5.13)**    | Generación de documentos PDF inmutables para tickets compatibles con impresoras térmicas POS comerciales (58mm/80mm). |
| **Flyway (SQL)**      | Control de versiones de la BD. Creación de los esquemas `V2` (Catálogo) y `V3` (Órdenes y Ventas). |
| **Bootstrap 5 y Fetch**| Maquetado Frontend web y peticiones asíncronas REST (Vanilla JS) para el control rápido de la interfaz "Touch" local. |

## 2. Clases y Componentes Desarrollados

| Módulo | Tipo de Capa | Clases / Archivos Creados | Descripción |
|--------|--------------|---------------------------|-------------|
| **Catálogo** | Dominio / Infra | `Category`, `Product` <br> `CategoryRepository`, `ProductRepository` | Entidades y repositorios (lectura) para consultar productos activos en el menú. |
| **Catálogo** | Presentación | `CatalogPublicAPI` | Controlador REST para exponer el menú filtrable (pastas, bebidas, etc) al Frontend. |
| **Órdenes** | Dominio | `Order`, `OrderLine`, `OrderStatus` | Entidades de gestión de venta con reglas de estado (Abierta/Cerrada/Cancelada). `Order` incluye versión para control de colisiones. |
| **Órdenes** | Infra / App | `OrderRepository`, `OrderLineRepository` <br> `OrderApplicationService` | Lógica core para instanciar órdenes, agregar líneas (calculando totales dinámicamente) y cancelación. |
| **Órdenes** | Presentación | `OrderPublicAPI`, `WaiterPOSController` <br> `waiter-pos.html` | Rutas API y Vista visual SPA del Mesero para navegar por categorías y conformar peticiones a la barra. |
| **Finanzas** | Dominio / Infra | `Ticket`, `SaleRepository` | Entidad bancaria inmutable de la venta final vinculada al identificador de la orden. |
| **Finanzas** | App (Service) | `SaleApplicationService`, `PdfReceiptGenerator` | Cierre estricto ("all or nothing" vía `@Transactional`) y motor gráfico para dibujar las líneas y subtotales en la memoria (Byte Array). |
| **Finanzas** | Presentación | `CashierPublicAPI`, `CashierPOSController` <br> `cashier-pos.html` | Pantalla Web del modulo Cajero. Escucha las cuentas de mesas pendientes, autoriza métodos de pago (Efectivo/Tarjeta) y fuerza descarga UI del Ticket en PDF. |

## 3. Problemáticas y Situaciones Atendidas

| Situación / Problema Encontrado | Descripción de la Causa / Análisis | Solución Aplicada |
|---------------------------------|------------------------------------|-------------------|
| **Justificación del Formato de Impresión** | Carga excesiva vs Fidelidad. Imprimir HTML directamente inyecta URLs deformando el rollo del ticket de hardware externo. | Se priorizó generar PDFs limpios desde el Backend de Java. Asegura un archivo final válido legalmente y directamente imprimible por colas de hardware local (Spoolers) independientemente del navegador web. |
| **JPA Schema-Validation Error** | Mapeo incompatible. En Java declaramos identificadores como `Long` mientras que en el script de migración SQL se usaba `SERIAL` e `INT`. | Actualización y refactorización de scripts `V2` y `V3` sustituyendo variables por `BIGSERIAL` y `BIGINT` en sentencias nativas en PostgreSQL. |
| **Fallo en el Escaneo de Repositorios (Beans)** | El contexto genérico de autoconfiguración de Spring Boot no localiza repositorios anidados dentro de una sola `Interface` contenedora. | Desacoplamiento de componentes aislando `ProductRepository` y `CategoryRepository` dentro del paquete `infrastructure` como clases independientes principales. |
| **TransientObjectException (JPA/Hibernate)** | Fuga de persistencia en cascada. Al grabar una Orden (`merge`), contenía nuevas líneas (`OrderLine`) sin un identificador validado aún. | Implementación explícita del manejador solitario `OrderLineRepository`, obligando a guardar directamente en base de datos la línea antes de vincularla al subtotal de la orden. |
| **Lógica fantasma de Creación de Archivos** | El Frontend insertaba erróneamente en automático nuevas órdenes en blanco sin validación del usuario al darle clic a "Descartar". Tampoco existía endpoint backend. | El Frontend se ajustó, forzando la creación de la nueva `Order` hasta que se pique el primer producto ("Perezosamente"). Además, en Backend se extendió la `OrderPublicAPI` respondiendo a un verbo `DELETE`. |
