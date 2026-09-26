Formato de cada entrada: fecha, objetivo, resultado, validación y siguiente paso.

## 2026-09-25 — Inventario inicial de Figma
- Revisé el árbol de capas y extraje texto del PDF `frontend/vehicle-rental.pdf`.
- Confirmé pantallas y contenido: KPIs/últimas rentas en Dashboard; clientes;
	vehículos en tabla/cuadrícula/estado vacío; stepper/detalle/devolución de
	rentas; pagos con confirmación; mantenimientos; usuarios.
- Discrepancias: Usuarios muestra contraseña manual y último acceso (el flujo
	acordado pide contraseña temporal y `User` no guarda último acceso); estados
	visuales de Vehículos no coinciden con `VehicleStatus`.
- Dashboard requiere definir cálculos/fuente para rentas activas, ingresos,
	flota disponible y rentas recientes. Los números del PDF son demostrativos.
- Siguiente: acordar esas tres diferencias de producto y luego implementar
	backend de Usuarios y el flujo obligatorio de cambio de contraseña.

## 2026-09-25 — Contexto y entorno alineados
- Reemplacé referencias al archivo de instrucciones con `contex.md` y
	aclaré que Figma es la fuente de verdad para las pantallas.
- Corregí versiones, rutas de Compose/entorno y `API_BASE_URL` para que
	apunte al origen real de los controladores (`http://localhost:8080`).
- Actualicé el roadmap para separar los módulos backend existentes del
	trabajo de convertir los frames de Figma en pantallas.
- Reconstruí solamente el contenedor `app`; PostgreSQL siguió activo y
	conservó sus datos. La imagen ahora inicia Spring Boot 3.4.3 con Java 21,
	conecta con la base y responde HTTP 200 en `/v3/api-docs`.
- Validación: Docker ejecutó `clean package`; 68 clases Java de producción
	y 1 clase de test compilaron. Docker omitió ejecutar pruebas mediante
	`-DskipTests`; localmente `mvn test` pasó (1 test, 0 errores) contra la
	base Docker. Esto valida el contexto de Spring, no todas las reglas de
	negocio. La compilación local también pasó.
- Siguiente: inventariar en Figma el flujo y las pantallas iniciales; luego
	implementar el rediseño de usuarios/autenticación antes de integrar la UI.
# Progress Log — Vehicle Rental System

Log cronológico de avances reales. Entradas más recientes ARRIBA.
Al agente: para retomar contexto, leé las últimas 2-3 entradas antes
de escanear el código. Actualizá este archivo vos mismo (el agente)
cada vez que se completa una tarea del DoD, en un formato consistente.

Formato de cada entrada: