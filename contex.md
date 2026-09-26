# Contexto del proyecto — Vehicle Rental System

## Sobre mí (para el agente)
Estoy usando este proyecto como último portafolio antes de pasar a React.
Mi objetivo principal es APRENDER, no solo terminar el proyecto. Priorizá
explicarme el porqué sobre darme la solución directa, salvo que la tarea
sea repetitiva (ver "Modo de trabajo" abajo).

## Stack
- Frontend: HTML/CSS/JS vanilla, ES Modules, arquitectura Multi-Page App
- Backend: Java 21 + Spring Boot 3.4.3, paquete `com.gregory.vehicleRentalAPI`
- DB: PostgreSQL
- Auth: JWT
- Diseño: ya terminado en Figma

## Estructura del repo (monorepo)
```
vehicle-rental-system/
├── .gitignore
├── README.md
├── ROADMAP.md   (alcance completo del proyecto)
├── PROGRESS.md  (log cronológico de avances)
├── contex.md    (contexto e instrucciones de trabajo)
├── backend/     (src, pom.xml, mvnw, Dockerfile, docker-compose.yml,
│                 .env.docker, .env.example, .gitattributes, ARCHITECTURE.md)
└── frontend/
    ├── pages/       (un .html por pantalla)
    ├── css/
    └── js/
        ├── api/         (clientes API, patrón factory)
        ├── components/  (sidebar y topbar inyectados por JS, sin duplicar)
        ├── pages/
        ├── utils/
        └── config.js    (API_BASE_URL -> http://localhost:8080)
```
Repo renombrado desde `vehicle-rental-api`. El backend se movió completo
a `/backend`; el paquete Java no cambió de nombre. La estructura del
frontend existe como scaffold, pero aún no contiene pantallas funcionales.

La UI ya está diseñada en Figma. Figma es la fuente de verdad visual y
funcional: no inventes pantallas ni módulos. Antes de implementar una
vista, identifica su frame, estados y flujo, y relaciónalos con los
endpoints existentes. Archivo: https://www.figma.com/design/3iUYCrRbS2zrBaSfo3jYVd/vehicle-rental?node-id=1-5
El inventario inicial está en ROADMAP.md; volver al archivo si hacen falta
medidas, copy o estados visuales que no figuren allí.

## Dónde mirar para más contexto
- **ROADMAP.md**: qué se espera construir en todo el proyecto (alcance
  completo). Cambia poco.
- **PROGRESS.md**: qué se hizo y cuándo, entrada más reciente arriba.
  Leer las últimas 2-3 entradas antes de escanear el código para
  entender en qué quedamos.
- **README.md**: descripción del monorepo, configuración y comandos para
  ejecutar backend y frontend.

## Cómo quiero que me ayudes (modo de trabajo)
1. Concepto NUEVO para mí (CORS, Security config, deploy, git avanzado,
   patrones de arquitectura, GitHub Projects/Kanban): explicá primero
   con una analogía simple, preguntame si quiero intentarlo yo antes
   de darme el código completo, y si me equivoco corregime explicando
   el porqué. No asumas que ya domino algo solo porque el proyecto es
   "avanzado".
2. Tarea REPETITIVA o que ya domino (CRUD similar a otro ya hecho,
   boilerplate de fetch, un componente de tabla igual a otro): código
   directo, sin rodeos.
3. Si no sabés si algo es nuevo para mí: preguntame antes de asumir.
4. Al terminar una feature, ayudame a revisarla con esta Definition
   of Done:
   - Funciona contra el backend real
   - Maneja errores básicos
   - Está bien commiteada
   - La probé manualmente
5. Cada vez que cerremos algo del Definition of Done, agregar una
   entrada nueva ARRIBA en PROGRESS.md (sin reescribir las viejas).

## Metodología de trabajo
- Kanban personal en GitHub Projects: Backlog / Esta semana / En
  progreso / Bloqueado / Hecho. Ver ROADMAP.md para el plan de
  aprendizaje detallado de esto (es concepto nuevo, tratar como tal).
- Ciclos semanales con meta clara y retro corta al final (qué bloqueó,
  qué aprendí, qué cambio para la próxima).
- Git: main/master estable, una rama por feature (nombrada en relación
  al issue), Conventional Commits (feat:, fix:, refactor:, docs:, chore:).
- Cerrar tarjetas del board automáticamente vía "Closes #N" en PR/commit.
- Deploy: aprender a desplegar backend (Docker) y frontend (estático)
  de forma incremental, no solo al final. Si algo ya funciona
  localmente, preguntame si quiero pausar y desplegarlo antes de seguir.

## Entorno y seguridad
- Spring Boot se versiona en `backend/pom.xml`; el contenedor debe
  construirse desde ese mismo proyecto y usar Java 21.
- Docker Compose se ejecuta desde la raíz con
  `-f backend/docker-compose.yml` y `--env-file backend/.env.docker`.
- Dentro de Compose, PostgreSQL se alcanza como `db:5432`; una ejecución
  local del backend usa `localhost:5432`.
- Spring/Maven no carga `.env` automáticamente. En IntelliJ hay que
  asociar `backend/.env` con EnvFile o exportar las variables al proceso.
- Nunca mostrar ni copiar valores secretos de `.env` o `.env.docker` en
  respuestas, logs o commits. No eliminar/recrear la base para reconstruir
  la API; primero evaluar y proteger cualquier dato existente.
- Preservar cambios locales no relacionados y no dar por completada una
  tarea solo porque exista su carpeta o configuración.

## Convenciones de arquitectura frontend acordadas
- MPA: un .html por pantalla en /frontend/pages
- ES Modules organizados en /js/api, /components, /pages, /utils
- Sidebar y topbar inyectados por JS (no duplicar HTML)
- Patrón factory para clientes de API
- Delegación de eventos para tablas dinámicas

## Cambio de flujo de autenticación (decidido, aún no implementado)
- Se elimina el registro público de empleados desde el login.
- Un ADMIN autenticado crea empleados desde el módulo Usuarios (POST /users).
- El sistema genera contraseña temporal al crear el usuario.
- Primer login del nuevo empleado fuerza cambio de contraseña
  (necesita flag `must_change_password` en backend + pantalla de
  "Cambiar contraseña" obligatoria en frontend).
- Cuando lleguemos a esto: diseñar completo en ambos lados, explicando
  las decisiones de seguridad detrás de cada parte.