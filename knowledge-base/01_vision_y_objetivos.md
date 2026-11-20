# Visión y Objetivos

> Fuente: `docs/discovery/discovery.md` e `docs/discovery/informe-discovery.md` (2026-11-20).

## Propósito del sistema

**Una agenda de turnos para consultorios odontológicos que no permite dobles turnos, ni del mismo odontólogo ni del mismo sillón.**

Los consultorios argentinos de 1 a 5 odontólogos con dos o más sillones dan los turnos a mano, por teléfono, WhatsApp, papel o planilla. Eso genera choques: el mismo profesional con dos pacientes a la vez, o dos profesionales usando el mismo sillón. Además, la secretaria pierde tiempo coordinando. En los 19 sistemas relevados en el Discovery, ninguno documenta públicamente la validación **conjunta** de profesional y sillón:
- Oclusar la hace solo por profesional.
- Órbita la hace solo por sillón.
- Odontosist bloquea los horarios ocupados del dentista.

Ese vacío es el centro del producto.

## Objetivos por actor

| Actor | Objetivo principal | Objetivos secundarios |
|---|---|---|
| Secretaria / recepción | Dar turnos sin generar choques de profesional, sillón ni paciente | Cancelar turnos y liberar el horario; avisar al paciente por WhatsApp sin escribir el mensaje a mano |
| Odontólogo | Ver su agenda del día | Saber en qué sillón atiende cada turno |
| Administrador / dueño | Configurar profesionales, sillones, prestaciones (con duración) y horario de atención | Gestionar usuarios y roles |

## Alcance v1.0 (MVP)

- Configuración de profesionales, sillones, prestaciones con duración fija y horario de atención del consultorio.
- Alta de pacientes con datos mínimos (siempre ficticios en ejemplos y tests).
- **Crear turno**: el fin se calcula con la duración de la prestación. Se rechazan los choques por profesional y por sillón, con un margen de 5 minutos. Un sobreturno explícito libera solo el choque del profesional. Además se valida que el paciente no tenga turnos superpuestos, que el turno no esté en el pasado y que caiga dentro del horario de atención (ver `05_reglas_de_negocio.md`).
- **Cancelar turno** con al menos 6 horas de anticipación, liberando el horario.
- **Agenda del día** por odontólogo.
- **Link de WhatsApp click-to-chat** (`wa.me`) con el mensaje del turno armado.
- Login con usuario y contraseña, con permisos por rol.
- Interfaz web con vistas Thymeleaf y una API REST.

## Fuera de alcance (v1.0)

Según la sección D del informe de Discovery:

- **Próxima etapa**: recordatorios y confirmación automáticos por WhatsApp Business API; ficha clínica y odontograma; cobros, seña con Mercado Pago y factura electrónica ARCA.
- **Etapas posteriores**: reprogramar turno; bloqueos y horario laboral por profesional; registro de asistencia y ausentes; reserva del paciente por link; lista de espera; obras sociales y prepagas; multisede.
- El paciente **no** es usuario del sistema en la v1.
- No se almacenan datos reales de pacientes en el repositorio, los ejemplos, los tests ni las capturas.

## Métricas de éxito

- **Correctitud**: cero turnos superpuestos por profesional o por sillón en los datos (salvo sobreturno de profesional), verificado con tests automáticos que cubren cada regla RN-TU.
- **Entrega académica**: el ciclo OPSX del primer change (crear turno sin solapamientos) queda archivado y con los tests en verde para el **2026-11-25**.
- **Producto (post-MVP, a validar)**: menos tiempo de la secretaria por turno dado. No se midió; ver SU-01 en `09_decisiones_y_supuestos.md`.
