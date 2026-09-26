# Roadmap — Vehicle Rental System

Este archivo describe el alcance del proyecto y sus próximos pasos.
Para el estado día a día, ver PROGRESS.md. Para contexto y forma de trabajo,
ver contex.md. La UI existente en Figma es la fuente de verdad para definir
las pantallas; no inventar requisitos visuales.

Estado de cada ítem: [ ] pendiente · [~] en progreso · [x] hecho y validado

## Backend

### Autenticación y Usuarios
- [x] Login con JWT
- [ ] Eliminar registro público de empleados desde el login
- [ ] ADMIN crea empleados desde módulo Usuarios (POST /users)
- [ ] Generación de contraseña temporal al crear usuario
- [ ] Flag `must_change_password` en backend
- [ ] Roles y permisos — CONFIRMAR ALCANCE (no especificado aún, pendiente
      de definir junto al diseño de Usuarios)
- [ ] Backend actual no tiene `UserController`: el registro público existe
      en `/auth/register` y el registro admin en `/auth/admin/register`.
      Alinear endpoints con el flujo final antes de construir el modal Figma.

### Módulos de dominio
- [x] Base backend para clientes, vehículos, rentas, pagos y
      mantenimientos implementada.
- [x] Inventario inicial de frames y asociación con los controllers
      existentes documentados en la sección "Inventario Figma".
- [ ] Revisar visualmente cada frame, variantes repetidas, permisos y
      estados de interacción antes de implementar las pantallas.
- [ ] Definir métricas y fuente de datos del Dashboard: actualmente no hay
      un controller/endpoints agregados para esa pantalla.

## Inventario Figma

Archivo: [vehicle-rental](https://www.figma.com/design/3iUYCrRbS2zrBaSfo3jYVd/vehicle-rental?node-id=1-5)
Páginas visibles: `design system` y `ui`.

| Área / frames observados | Backend existente | Nota de integración |
|---|---|---|
| Login | `POST /auth/login` | Endpoint existente; integrar después de definir el flujo de primer acceso. |
| Modal - Registrar usuario, módulo Usuarios | No existe `POST /users` | Figma pide contraseña manual y muestra último acceso; el flujo acordado pide contraseña temporal generada. `User` tampoco guarda último acceso. Acordar diferencias antes de implementar. |
| Dashboard | Sin endpoint de dashboard | PDF muestra rentas activas, ingresos, flota disponible y rentas recientes. Definir fuente/cálculo y si se agregará un endpoint agregado. |
| Clientes (3 variantes), Nuevo cliente | `/api/customers` CRUD | Revisar diferencias entre variantes y estados. |
| Vehículos: Tabla, Cuadrícula, Estado Vacío, Nuevo Vehículo - Drawer | `/api/vehicles` CRUD | PDF muestra búsqueda, tabla y tarjetas. Algunas etiquetas de estado (`ACTIVA`, `PENDIENTE`, `VENCIDA`) no coinciden con `VehicleStatus` (`AVAILABLE`, `RENTED`, `MAINTENANCE`, `INACTIVE`); validar qué significan. |
| Rentas - Listado, Detalle de Renta, Nueva Renta - Stepper, Modal - Devolver Vehículo | `/api/rentals` | Stepper: cliente, vehículo, fechas y facturación; detalle incluye pagos y progreso; devolución captura fecha, kilometraje, inspección y saldo. Devolución/cancelación usan PATCH. |
| Pagos, Modal - Registrar pago, confirmación | `/api/payments` | Tabla con filtros por estado/método/renta, confirmar/cancelar y registro con saldo anterior/posterior. |
| Mantenimiento, Modal - Registrar mantenimiento | `/api/maintenances` | Tabla con filtros por estado/vehículo y formulario vehículo, descripción, costo y fecha estimada. |
| Button, Toast, Modal, Table with pagination (2 variantes) | No aplica | Componentes/variantes de UI; no son módulos de negocio. |

El PDF es una página con varias pantallas compuestas. Sus valores numéricos
son datos de ejemplo, no requisitos ni datos reales. Antes de implementar,
validar copy, dimensiones, permisos, transiciones y variantes visuales en
los frames correspondientes.

### Infraestructura backend
- [x] Monorepo: backend en `/backend`; paquete Java conservado.
- [x] Imagen Docker reconstruida desde el POM actual; Spring Boot 3.4.3
      inicia y conecta con PostgreSQL. La primera consulta OpenAPI responde 200.
- [x] `application.properties` no depende de rutas relativas; usa
      variables de entorno documentadas.
- [x] CORS configurado para Live Server en `http://localhost:5500`.
- [x] Test de contexto Maven pasa contra PostgreSQL (1 test); ampliar
      cobertura de reglas de negocio junto con futuras features.
- [ ] Persistencia PostgreSQL con volumen nombrado. Antes de aplicarlo,
      respaldar la base actual: Compose aún no monta un volumen.

## Frontend

### Arquitectura acordada
- [x] Scaffold de carpetas creado: `/frontend/pages`, `/css`,
      `/js/{api,components,pages,utils}`.
- [x] `API_BASE_URL` apunta al origen `http://localhost:8080` (los
      controladores no usan un prefijo global `/api`).
- [ ] Patrón Multi-Page App: un .html por pantalla en /pages
- [ ] Sidebar y topbar inyectados por JS (evitar duplicar HTML en cada página)
- [ ] Patrón factory para clientes de API en /js/api
- [ ] Delegación de eventos para tablas dinámicas
- [ ] Pantalla de login
- [ ] Pantalla obligatoria de "Cambiar contraseña" (primer login)
- [ ] Pantallas de dominio según el inventario y prioridad acordados desde Figma

## Flujo de autenticación (rediseño decidido)
- [ ] Ver detalle completo en sección "Usuarios" arriba. Diseñar en ambos
      lados (backend + frontend) explicando decisiones de seguridad
      cuando se llegue a esta feature.

## Documentación
- [x] README raíz con versiones, rutas de Compose y configuración de entorno
- [ ] Revisar documentación específica del backend si el README raíz deja
      de ser suficiente para sus comandos y operación

## Despliegue (incremental, no solo al final)
- [ ] Backend dockerizado desplegado — CONFIRMAR proveedor
- [ ] Frontend estático desplegado — CONFIRMAR proveedor
- [ ] Checkpoint: cada vez que algo funcione localmente end-to-end,
      evaluar pausar y desplegar antes de seguir sumando features

## Metodología y forma de trabajo (también son "entregables" del proyecto,
## porque el objetivo es aprenderlos, no solo producir código)

### GitHub Projects (Kanban personal) — tratar como concepto NUEVO
La primera vez que se use, no asumir que ya se domina. Enseñar con
explicación + práctica guiada, no solo pasos a ejecutar sin entender.
- [ ] Explicar qué es un "Project" en GitHub y cómo se vincula a issues
      del repo
- [ ] Explicar la diferencia entre una tarjeta CON issue asociado y
      una tarjeta SIN issue asociado (y cuándo conviene cada una)
- [ ] Configurar el tablero desde cero: columnas Backlog / Esta semana /
      En progreso / Bloqueado / Hecho
- [ ] Practicar: convertir una feature grande (ej. "módulo de Usuarios")
      en tareas pequeñas y accionables dentro del tablero — no una
      tarjeta gigante
- [ ] Correr un primer ciclo semanal completo con meta clara
- [ ] Al cerrar el ciclo, guiar una retro corta respondiendo:
      (1) qué bloqueó, (2) qué aprendí, (3) qué cambio para la próxima
      — y reflejar esos ajustes en el tablero

### Git en conjunto con el tablero
- [ ] Rama main/master siempre estable
- [ ] Una rama por feature/tarjeta
- [ ] Explicar cuándo crear la rama (al mover la tarjeta a "En progreso")
      y cómo nombrarla en relación al issue (ej. `feature/12-modulo-usuarios`)
- [ ] Conventional Commits (feat:, fix:, refactor:, docs:, chore:)
- [ ] Cerrar tarjeta automáticamente al mergear usando "Closes #N" en
      el commit o PR

### Ciclo de trabajo por feature
- [ ] Definition of Done aplicado a cada feature antes de darla por
      cerrada: funciona contra el backend real, maneja errores básicos,
      está bien commiteada, la probé manualmente

## Fuera de alcance (explícitamente)
- Migración a React: es el PRÓXIMO proyecto, no parte de este.
- Cambios a módulos backend funcionales que no sean necesarios para el
      nuevo flujo de usuarios o para integrar la UI diseñada.

## Pendientes de definición (no inventar, preguntar cuando se llegue)
- Prioridad/orden de pantallas y definición de métricas del Dashboard.
- Si el alta de Usuarios debe generar contraseña temporal o aceptar una
      contraseña manual como aparece en el PDF.
- Correspondencia entre estados de Vehículos en Figma y `VehicleStatus`.
- Matriz de permisos por rol según el diseño y las reglas del negocio.
- Proveedores de despliegue para backend y frontend.