# Skill Registry

**Delegator use only.** Any agent that launches sub-agents reads this registry to resolve compact rules, then injects them directly into sub-agent prompts. Sub-agents do NOT read this registry or individual SKILL.md files.

Project: Sistema Odontologico. Stack: Java 17, Spring Boot 3, Maven Wrapper, Spring Data JPA + H2, Thymeleaf, Spring Security, JUnit 5. Workflow: OpenSpec SDD (OPSX).

## User Skills

Relevance: YES = apply to this project; SUPPORT = workflow/foundation skill; NO = other stack, do not inject.

| Trigger | Skill | Relevance | Path |
|---------|-------|-----------|------|
| Adding/changing Spring Boot 3/4 features (controllers, validation, ProblemDetail, JPA) | java-springboot | YES | ~/.claude/skills/java-springboot/SKILL.md |
| Writing/reviewing/fixing JUnit 5, AssertJ, Mockito tests | java-junit | YES | ~/.claude/skills/java-junit/SKILL.md |
| Spring Security authn/authz, CSRF, validation, secrets, headers, rate limiting | springboot-security | YES | ~/.claude/skills/springboot-security/SKILL.md |
| Designing/reviewing REST APIs | api-design-principles | YES | ~/.claude/skills/api-design-principles/SKILL.md |
| Test-first development, red-green-refactor | tdd | YES | ~/.claude/skills/tdd/SKILL.md |
| openspec apply / implement tasks | openspec-apply-change | YES | .claude/skills/openspec-apply-change/SKILL.md |
| openspec archive | openspec-archive-change | YES | .claude/skills/openspec-archive-change/SKILL.md |
| openspec explore | openspec-explore | YES | .claude/skills/openspec-explore/SKILL.md |
| openspec propose | openspec-propose | YES | .claude/skills/openspec-propose/SKILL.md |
| openspec sync specs | openspec-sync-specs | YES | .claude/skills/openspec-sync-specs/SKILL.md |
| openspec update change | openspec-update-change | YES | .claude/skills/openspec-update-change/SKILL.md |
| Visual identity / design system (policy currently deferred) | design-system | SUPPORT | ~/.claude/skills/design-system/SKILL.md |
| UI polish, animation (only if Thymeleaf UI is built) | emil-design-eng | SUPPORT | ~/.claude/skills/emil-design-eng/SKILL.md |
| Frontend design taste (only if UI is built) | design-taste-frontend-v1 | SUPPORT | ~/.claude/skills/design-taste-frontend-v1/SKILL.md |
| Foundation flow: discovery, KB, roadmap, find/create skills, scraping, instructions | discovery-research, kb-creator, roadmap-generator, find-skill, web-scraper, skill-creator, active-orchestrator, agent-instruction | SUPPORT | ~/.claude/skills/<name>/SKILL.md |
| Fastify Node.js servers | fastify-best-practices | NO (other stack) | ~/.claude/skills/fastify-best-practices/SKILL.md |
| Vitest unit tests (JS/TS) | vitest | NO (other stack) | ~/.claude/skills/vitest/SKILL.md |

## Compact Rules

### java-springboot
- Detect versions/conventions from pom.xml first; never upgrade Boot/Java/deps implicitly; no new infrastructure (broker, cache, DB) without asking.
- Package by feature; constructor injection; controllers return DTOs/records, never entities; services own the @Transactional boundary.
- Bean Validation on request records with @Valid; one @RestControllerAdvice returning ProblemDetail.
- Boot 3 uses Jackson 2 (com.fasterxml.jackson.databind, ObjectMapper) and spring-boot-starter-web.
- Put @EnableJpaAuditing/@EnableCaching/@EnableAsync/@EnableScheduling on dedicated @Configuration classes, not on @SpringBootApplication (slice tests like @DataJpaTest break); @Import them in slices.
- @Transactional/@Cacheable/@Async work via proxies: no effect on self-invocation or private/final methods.
- No secrets in code, properties or logs; verify with ./mvnw verify.

### java-junit
- Copy existing test conventions; do not add AssertJ/Mockito/JaCoCo to a project lacking them without asking; never bump JUnit major implicitly.
- Assert outcomes/state, not internal call counts; avoid bare assertNotNull; never catch AssertionError.
- Mock only boundary collaborators (repository, client, clock); never mock records/value objects/collections/class under test; prefer fakes for stateful ones; avoid mockStatic.
- MockitoExtension strict stubs: fix unused stubs instead of going LENIENT.
- Determinism: inject java.time.Clock (Clock.fixed) and fixed-seed Random; no sleeps, no default locale/timezone dependence (project zone: America/Argentina/Buenos_Aires).
- Unit tests *Test via Surefire; integration *IT via Failsafe (mvn verify).
- NEVER delete, @Disabled, weaken or skip a failing test, nor use -DskipTests; if the fix changes production behavior, ask the user.
- Run narrowest first (./mvnw -q -Dtest=XTest test), then ./mvnw verify. Flaky tests: remove nondeterminism, no retries.
- Cover happy path, boundaries and errors for new code.

### springboot-security
- Browser session app (Thymeleaf): keep CSRF enabled and include the token in forms; disable CSRF only for stateless Bearer-token APIs.
- Sessions: httpOnly, Secure, SameSite=Strict cookies.
- Authorization: @EnableMethodSecurity, @PreAuthorize("hasRole('X')"); deny by default, expose only required endpoints.
- Validate all input with Bean Validation (@Valid, @NotBlank, @Size on DTOs); sanitize HTML before rendering.
- SQL: Spring Data derived queries or :param bindings; never concatenate strings into queries.
- Passwords: PasswordEncoder bean (BCrypt cost 12 / Argon2); never plaintext.
- Secrets via environment variables, never in the repo; add security headers, rate limit login, scan dependencies for CVEs.

### api-design-principles
- Resources are plural nouns (/turnos, /profesionales); actions via HTTP methods (GET safe, POST create, PUT replace, PATCH partial, DELETE).
- Correct status codes: 2xx success (201 on create), 4xx client errors (409 for conflicts like turn overlap), 5xx server.
- Stateless requests; paginate collections; plan versioning (e.g. /api/v1); document with OpenAPI.
- Consistent, structured error bodies (ProblemDetail).

### tdd
- Red before green: failing test first, then minimal code; one test -> one implementation per cycle (vertical slices, no bulk tests first).
- Test behavior through public interfaces/seams; agree seams with the user before writing tests.
- Avoid implementation-coupled and tautological tests; expected values come from the spec or known literals.
- Refactoring is outside the red-green loop.

### openspec-* (apply, archive, explore, propose, sync-specs, update-change)
- The openspec CLI owns artifact state: always run `openspec status` / `openspec list` / `openspec instructions`; never guess or create openspec/ structure manually.
- propose generates proposal, specs, design, tasks in one step; update-change only revises planning artifacts and never edits code.
- apply implements tasks from the change and checks them off; archive finalizes a completed change; sync-specs merges delta specs into main specs.
- explore is thinking-only: no code changes.

### design-system / emil-design-eng / design-taste-frontend-v1
- Visual policy is currently `deferred`: at the first visual change, invoke design-system to ask once for the project-wide mode (system-and-mockups | system-only | off).
- Before UI apply, check design-system/README.md Approval in system modes; per-change mockups only in system-and-mockups mode.

## Project Conventions

| File | Path | Notes |
|------|------|-------|
| (none yet) | - | AGENTS.md / CLAUDE.md not created yet (agent-instruction step pending) |
| Knowledge base | knowledge-base/README.md | Project KB (10 files), pointed to by CHANGES.md "Leer antes" |
| Roadmap | CHANGES.md | Change index |

Read the convention files listed above for project-specific patterns and rules.
