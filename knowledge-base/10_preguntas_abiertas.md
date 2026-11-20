# Preguntas Abiertas

## Inconsistencias detectadas

### IN-01: Thymeleaf + JWT
**Decisión A dice**: interfaz web con vistas Thymeleaf renderizadas en el servidor (DD-05).
**Decisión B dice**: autenticación con login de usuario y contraseña y JWT (ronda técnica, 4b).
**Impacto**: JWT es natural para APIs consumidas por otro cliente (SPA o móvil). Con Thymeleaf, el navegador no envía un header `Authorization` en cada navegación sin JavaScript adicional, y guardar el token en una cookie obliga a resolver CSRF de todos modos.
**Resolución propuesta**: form login con **sesión** de Spring Security para las vistas Thymeleaf. JWT **solo** para `/api/**`, si se decide exponer la API a otro cliente. Decidirlo en el change de autenticación, que no es el primero (DD-06).

### IN-02: Tamaño del equipo vs. la consigna
**La consigna dice**: "Individual o en grupo de hasta 3 integrantes".
**El equipo informó**: 6 personas.
**Impacto**: puede afectar la evaluación de "trazabilidad y participación".
**Resolución propuesta**: confirmar con la cátedra antes de la entrega.

### IN-03: Estructura de carpetas sugerida vs. Maven
**La consigna sugiere**: `src/` y `tests/` en la raíz.
**Maven usa**: `src/main/java` y `src/test/java`.
**Resolución propuesta**: seguir Maven (DD-07) y aclararlo en el README.

## Preguntas abiertas (priorizadas)

| ID | Prioridad | Pregunta | Bloquea | Decisor |
|---|---|---|---|---|
| PA-01 | **Alta** | Instalar el JDK 17 en la máquina del líder y en la de cada integrante (hoy no hay `java`, `mvn` ni `gradle`). ¿Qué distribución (Temurin, por ejemplo)? | Apply del primer change | Equipo técnico |
| PA-03 | **Alta** | ¿El margen de 5 minutos se aplica también entre un turno y su sobreturno con el mismo profesional? Supuesto actual: no (SU-04) | Spec del primer change | Product Owner (líder) |
| PA-06 | **Alta** | Concurrencia: ¿alcanza con la transacción y la revalidación, o se requiere un lock pesimista o aislamiento serializable para que dos secretarias no creen el mismo choque a la vez? | Diseño del primer change | Equipo técnico |
| PA-04 | Media | ¿Quién puede marcar un sobreturno: solo la secretaria y el admin, o también el odontólogo? Supuesto actual: secretaria y admin | Spec del primer change | Product Owner |
| PA-02 | Media | ¿El horario de atención es único o varía por día? Supuesto: una franja por día (SU-03) | Change de configuración | Product Owner |
| PA-07 | Media | ¿Los turnos se crean en múltiplos de 5 minutos o en cualquier minuto? Supuesto: cualquiera (SU-05) | Spec del primer change | Product Owner |
| PA-05 | Baja | ¿Los feriados bloquean la agenda? Supuesto: no en la v1 (SU-06) | Etapas posteriores | Product Owner |
| PA-08 | Baja | Si Argentina volviera a aplicar horario de verano, ¿cómo se tratan los turnos en el cambio de hora? | — | Equipo técnico |
| PA-09 | Baja | IN-01: ¿sesión o JWT para las vistas? | Change de autenticación | Equipo técnico |

## Datos "No evidenciado" del Discovery

Datos que la investigación de mercado (2026-11-20) no pudo respaldar con fuentes públicas. Conviene validarlos con demos (informe D.1) antes de usarlos como argumento comercial:

| Tema | Sistemas sin evidencia pública | Por qué importa |
|---|---|---|
| Precios | Bilog, Dentalink, Órbita, ClinIA, Curve, Dentidesk (solo cotización) | El diferencial "precio publicado" (DD-01) se apoya en esta opacidad |
| Prevención de solapamientos | Todos salvo Oclusar (profesional), Órbita (sillón), Odontosist (dentista) y Dentiqa (solo comercial) | Es la base del diferencial; si otros lo hacen sin documentarlo, el vacío es menor |
| Validación conjunta profesional + sillón | Ningún sistema relevado | Ídem; validar en demos de Órbita y Oclusar |
| Duración variable de turnos | Bilog (arrastrar sí), Oclusar, Dentalink | Referencia para RN-TU-01 |
| Sillones/boxes en la agenda | Bilog, Dentalink, Dentia, Odonto.ar | Referencia de modelo de datos |
| Normativa argentina declarada (Leyes 25.326 y 26.529) | Bilog, Órbita, Dentiqa | Referencia de cumplimiento |
| Backup | Bilog, Dentia, Órbita, Odonto.ar | Referencia de seguridad |
| Sitios sin evidencia legible | Doctoralia AR (sitio caído), Reservo (solo JavaScript) | Quedaron fuera de la comparación |
| Adopción real | Cifras como "+15.000 clientes" (Dentalink) o "+30.000 clínicas" (AgendaPro) son afirmaciones comerciales | No usarlas como dato de mercado sin verificar |
