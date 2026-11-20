# Arquitectura Propuesta

## Patrones aplicados

| Patrón | Dónde se usa | Por qué |
|---|---|---|
| Capas con dominio aislado (hexagonal liviano) | `domain` (Java puro) ← `application` ← `infrastructure`/`web` | Las reglas de choque se testean con JUnit sin Spring y no dependen de JPA |
| Caso de uso (application service) | `CrearTurno`, `CancelarTurno`, `ConsultarAgenda`, `GenerarLinkWhatsApp` | Un punto de entrada por operación, con su transacción |
| Repository (puertos) | Interfaces en `domain`, implementaciones con Spring Data JPA en `infrastructure` | Permite cambiar H2 por PostgreSQL y usar dobles en los tests |
| Value object | `Intervalo` (inicio, fin, choca con margen), `Telefono` | Concentra la lógica de superposición en un solo lugar, testeable |
| Excepción de dominio con código | `ReglaDeNegocioException(codigo)` → manejador global → Problem Details | Cada error lleva el código RN y la API es trazable |
| Clock inyectable | Bean `Clock` (zona del consultorio) | Tests deterministas de "pasado" y "6 horas antes" |
| Puerto de notificación | `NotificadorDeTurnos` (implementación MVP: generador de link wa.me) | Permite enchufar WhatsApp Business API después sin tocar el dominio |

## Estructura de directorios

```
turnos-odontologia/
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/            # Maven Wrapper: no requiere Maven instalado
├── src/main/java/ar/odonto/turnos/
│   ├── TurnosApplication.java
│   ├── domain/
│   │   ├── model/                   # Turno, Profesional, Sillon, Prestacion, Paciente, HorarioAtencion, Intervalo
│   │   ├── rules/                   # ValidadorDeTurno, reglas RN-TU / RN-CA
│   │   ├── port/                    # TurnoRepository, ..., NotificadorDeTurnos
│   │   └── error/                   # ReglaDeNegocioException, CodigoError
│   ├── application/                 # CrearTurno, CancelarTurno, ConsultarAgenda, GenerarLinkWhatsApp
│   ├── infrastructure/
│   │   ├── persistence/             # entidades JPA y adaptadores de repositorio
│   │   ├── whatsapp/                # WaMeLinkGenerator
│   │   └── config/                  # Clock, seguridad, seed (perfil dev)
│   └── web/
│       ├── api/                     # @RestController + DTOs + manejador de errores (Problem Details)
│       └── view/                    # @Controller de las vistas Thymeleaf
├── src/main/resources/
│   ├── application.yml              # perfil por defecto: H2 en memoria
│   ├── application-dev.yml          # seed ficticio y consola H2
│   ├── templates/                   # vistas Thymeleaf
│   └── static/
└── src/test/java/ar/odonto/turnos/
    ├── domain/                      # tests unitarios de reglas (uno por escenario del spec)
    ├── application/                 # tests de casos de uso con repositorios en memoria o JPA
    └── web/                         # @WebMvcTest / @SpringBootTest de la API
```

> La consigna sugiere `src/` y `tests/` en la raíz. En Maven los tests van en `src/test/java` por convención; se documenta en el README (DD-07).

## Seguridad

- **Autenticación**: Spring Security con usuario y contraseña (BCrypt). Mecanismo de sesión o token: ver **IN-01**. Propuesta: form login con sesión para las vistas Thymeleaf; JWT solo si la API se consume desde fuera.
- **Autorización**: por rol (`ADMIN`, `SECRETARIA`, `ODONTOLOGO`), con `@PreAuthorize` en los casos de uso o reglas por ruta. Chequeo de propiedad para la agenda del odontólogo (RN-AG-02).
- **Validación de entrada**: Bean Validation en los DTOs (formato) y reglas de negocio en el dominio (semántica). Nunca confiar en un `fin` enviado por el cliente.
- **Datos personales**: datos ficticios en todo el repositorio (RN-DP-01) y sin datos de pacientes en los logs (RN-DP-03).
- **Gestión de secretos**: nada sensible en el repositorio. El secreto de firma JWT (si se usa) y las contraseñas de seed salen de variables de entorno. Hay que agregar `.gitignore` para `.env`, `target/` y archivos de IDE.
- **Consola H2**: habilitada solo con el perfil `dev`.

## Variables de entorno

| Variable | Descripción | Ejemplo | Sensible |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` | N |
| `TURNOS_MARGEN_MINUTOS` | Margen entre turnos (RN-TU-02 y RN-TU-03) | `5` | N |
| `TURNOS_CANCELACION_HORAS_MINIMAS` | Anticipación mínima para cancelar (RN-CA-01) | `6` | N |
| `TURNOS_ZONA_HORARIA` | Zona del consultorio | `America/Argentina/Buenos_Aires` | N |
| `JWT_SECRET` | Clave de firma del token (solo si se usa JWT, IN-01) | (no versionar) | **S** |
| `SEED_ADMIN_PASSWORD` | Contraseña del admin de seed en `dev` | (no versionar) | **S** |
| `SPRING_DATASOURCE_URL` | Para pasar a PostgreSQL en el futuro | `jdbc:h2:mem:turnos` | N (S si lleva credenciales) |

## Testing y verificación

- **Un test automatizado por escenario del spec** (consigna, etapa 6): feliz, error de negocio y borde.
- Tests de dominio con JUnit 5 y AssertJ, sin Spring y con `Clock.fixed(...)`.
- Tests de integración de la API con `@SpringBootTest` y MockMvc sobre H2.
- **Health check**: `spring-boot-starter-actuator` con `/actuator/health`.
- Comando único: `./mvnw test` (o `mvnw.cmd test` en Windows).
