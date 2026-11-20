# Actores y Roles

## Actores del sistema

| Actor | Descripción | Cómo interactúa |
|---|---|---|
| Secretaria / recepción (`SECRETARIA`) | Coordina la agenda del consultorio; atiende teléfono y WhatsApp | Vistas web y API: da de alta pacientes, crea y cancela turnos, abre el link de WhatsApp |
| Odontólogo (`ODONTOLOGO`) | Atiende a los pacientes; está asociado a un registro de `Profesional` | Vista web: consulta **su** agenda del día |
| Administrador / dueño (`ADMIN`) | Dueño o responsable del consultorio | Vistas web y API: configura profesionales, sillones, prestaciones, horario y usuarios; además puede hacer todo lo de la secretaria |
| Paciente | Persona que recibe la atención | **No es usuario en la v1.** Solo recibe el mensaje de WhatsApp que envía la secretaria |
| Reloj del sistema (`Clock`) | Actor técnico | Define el "ahora" para las reglas de pasado y de anticipación. Se inyecta para poder testearlo |

## RBAC: matriz de permisos

C = crear · L = leer · M = modificar · B = baja lógica · — = sin acceso

| Recurso | ADMIN | SECRETARIA | ODONTOLOGO |
|---|---|---|---|
| Usuarios | CLMB | — | — |
| Profesionales | CLMB | L | L |
| Sillones | CLMB | L | L |
| Prestaciones | CLMB | L | L |
| Horario de atención | LM | L | L |
| Pacientes | CLM | CLM | L (solo los de sus turnos) |
| Turnos: crear | C | C | — |
| Turnos: cancelar | M | M | — |
| Turnos: marcar sobreturno | C | C | — (ver PA-04) |
| Agenda del día | L (todas) | L (todas) | L (solo la propia) |
| Link de WhatsApp | L | L | — |

Reglas de autorización relacionadas: RN-AU-01 a RN-AU-04.

## Rutas públicas

- `GET /login` (vista de login) y `POST /api/auth/login`.
- Recursos estáticos (`/css/**`, `/js/**`, `/img/**`).
- `GET /actuator/health` (health check usado en la fase Verify).
- Consola H2 (`/h2-console`): **solo** con el perfil `dev`, nunca en producción.

Todo lo demás requiere autenticación.
