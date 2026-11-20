# Discovery: Sistema de turnos para consultorios odontológicos

**Fecha**: 2026-11-20
**Fuentes investigadas**: 19 sistemas (ver `docs/discovery/sources/fichas-competidores.md`), consultados el 2026-11-20
**Método**: investigación de mercado sobre sitios oficiales y cuatro rondas de preguntas con el equipo (checklist de 11 puntos de `discovery-research`)

## 1. Problema que resuelve

Los consultorios odontológicos argentinos de 1 a 5 profesionales con dos o más sillones dan los turnos a mano, por teléfono y WhatsApp. Eso produce **dobles turnos**: el mismo odontólogo o el mismo sillón quedan ocupados a la vez. Además, la secretaria pierde tiempo coordinando la agenda.

En los 19 sistemas relevados, la prevención de choques aparece documentada solo en una dimensión por producto: Oclusar la hace por profesional, Órbita por sillón y Odontosist bloquea los horarios ocupados del dentista. Ninguno documenta públicamente la validación conjunta de **profesional y sillón**.

## 2. Usuarios / roles

- **Secretaria / recepción**: da y cancela turnos.
- **Odontólogo**: consulta su agenda del día.
- **Administrador / dueño**: configura profesionales, sillones, prestaciones y el horario de atención.
- **Paciente**: no usa el sistema en la v1. La autogestión queda para etapas posteriores.

## 3. Casos de uso

1. Como secretaria, quiero dar un turno y que el sistema rechace los choques de profesional o de sillón, para no generar dobles reservas.
2. Como secretaria, quiero cancelar un turno para liberar el horario.
3. Como odontólogo, quiero ver mi agenda del día para organizarme.
4. Como administrador, quiero cargar prestaciones con su duración, profesionales y sillones, para que la agenda calcule los turnos.

## 4. Competidores / soluciones existentes

| Competidor | País | Qué resuelve | Pricing (público) | Diferenciador evidenciado |
|---|---|---|---|---|
| Dentia | AR | Gestión integral con IA en WhatsApp | ARS 20.000–40.000/mes | Facturación ARCA A/B/C, bot que agenda y cancela |
| Órbita | AR | Chat de IA en WhatsApp + gestión clínica | A cotizar | Agenda por sillón que impide el choque |
| Oclusar | AR | Gestión simple para consultorio | "Empezás gratis"; montos no publicados | La base de datos impide el doble turno del profesional |
| AgendaPro | LatAm (landing AR) | Agenda de salud y estética | ARS 9.900–129.900/mes | Agenda por sillón + seña con Mercado Pago |
| Bilog | AR | Gestión odontológica con más de 20 años | No publicado | Agenda visual y liquidación de obras sociales |

La tabla completa de los 19 sistemas, con la matriz de puntuación, está en `docs/discovery/informe-discovery.md`.

**Notas**: el estándar de mercado es agenda + recordatorios por WhatsApp + odontograma. Lo local diferencial es ARCA, obras sociales y Mercado Pago. Los vacíos más claros son la validación conjunta de profesional y sillón, la falta de precios publicados (Bilog, Dentalink, Órbita, ClinIA y Curve no los publican) y los recordatorios que no salen solos (Oclusar).

## 5. Funcionalidades necesarias (MVP)

- Crear turno con la duración tomada de la prestación, sin choques por profesional ni por sillón.
- Cancelar turno y liberar el horario.
- Agenda del día por odontólogo.
- Link de WhatsApp click-to-chat (wa.me) con el mensaje del turno armado, para que la secretaria lo envíe a mano.

## 6. Funcionalidades opcionales

**Próxima etapa (deseadas):**
- Recordatorios y confirmación automáticos por WhatsApp.
- Ficha clínica y odontograma.
- Cobros, seña con Mercado Pago y factura electrónica ARCA.

**Etapas posteriores:**
- Reprogramar turno.
- Bloqueos y horario laboral de cada profesional.
- Registro de asistencia y ausentes.
- Reserva del paciente por link.
- Lista de espera.
- Obras sociales y prepagas.

## 7. Reglas de negocio

- **R1. Duración**: el fin del turno es el inicio más la duración fija de la prestación.
- **R2. Margen**: entre dos turnos del mismo profesional o del mismo sillón tiene que haber al menos 5 minutos. Si un turno termina a las 10:00, el siguiente puede empezar a las 10:05; a las 10:04 se rechaza.
- **R3. Sillón**: todo turno ocupa un sillón. Un sillón nunca tiene dos turnos superpuestos, tampoco con sobreturno.
- **R4. Sobreturno**: un turno marcado explícitamente como sobreturno puede chocar con otro turno del mismo profesional. Nunca puede chocar por sillón.
- **R5. Paciente**: un paciente no puede tener dos turnos superpuestos.
- **R6. Validez temporal**: no se puede crear un turno en el pasado ni fuera del horario de atención del consultorio.
- **R7. Cancelación**: hacen falta al menos 6 horas de anticipación. Un turno cancelado no cuenta para los choques.
- **R8. Zona horaria**: America/Argentina/Buenos_Aires.

## 8. Integraciones

- **MVP**: solo el link de WhatsApp click-to-chat (wa.me), sin API ni costo.
- **Posteriores**: WhatsApp Business API, Mercado Pago y ARCA. Se diseñan como interfaces para enchufarlas después.

## 9. Restricciones

- Entrega final: **2026-11-25**.
- Equipo de 6 personas (la consigna dice "hasta 3 integrantes"; hay que confirmarlo con la cátedra).
- Solo datos ficticios de pacientes, en ejemplos, tests y capturas.
- Marco legal de datos de salud: Ley 25.326 (protección de datos personales) y Ley 26.529 (derechos del paciente e historia clínica).
- Alcanza con una API o un módulo de dominio; la interfaz gráfica no es obligatoria.
- Sin secretos ni credenciales en el repositorio.

## 10. Riesgos

- **Riesgo principal (equipo)**: no llegar al 25/11 con el ciclo OPSX completo (explore → archive).
- **Supuesto sin probar**: que el doble turno sea el dolor principal de los consultorios. No se validó con odontólogos reales.
- **Supuesto sin probar**: que las funcionalidades de los competidores sean reales. Buena parte de la evidencia son afirmaciones comerciales o maquetas.
- **Riesgo**: la evidencia de algunos productos sale de una sola página (Dentia, OdontoGRAMA, OdontoPlus), lo que puede inflar o desinflar su puntaje.

## 11. Preguntas abiertas

- ¿El horario de atención es único para el consultorio o varía por día de la semana?
- ¿El margen de 5 minutos se aplica también entre un turno y su sobreturno?
- ¿Quién puede marcar un sobreturno: solo la secretaria o también el odontólogo?
- ¿Los feriados bloquean la agenda?
- ¿Los turnos se crean en múltiplos de 5 minutos o en cualquier minuto?
- ¿El equipo de 6 personas es compatible con la consigna?
- Datos que la investigación dejó como **No evidenciado**:
  - Precios de Bilog, Dentalink, Órbita, ClinIA, Curve y Dentidesk.
  - Prevención de solapamientos en la mayoría de los sistemas (solo hay evidencia en Oclusar, Órbita, Odontosist y Dentiqa, esta última comercial).
  - Normativa argentina declarada por Bilog, Órbita y Dentiqa.
  - Duración variable de los turnos en Bilog, Oclusar y Dentalink.
  - Sitios de Doctoralia AR (no respondió) y Reservo (contenido no legible).
