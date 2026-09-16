# AgyDeck Repository Review Standards

This repository enforces strict Clean Architecture, idiomatic Kotlin 2.1, and quality standards for AgyDeck.

---

## 1. Clean Architecture Layer Boundaries

Dependencies must flow strictly inward:
$$\text{:presentation} \longrightarrow \text{:application} \longrightarrow \text{:domain} \longleftarrow \text{:infrastructure}$$

- **`:domain` (Pure Kotlin Layer)**:
  - Contains core domain entities, value objects, and domain errors.
  - **Zero platform or SDK dependencies**: No Android APIs, Compose UI, Ktor, or framework packages.
  - Invariants must be enforced on creation (e.g. `@JvmInline value class` validation).
  - Domain errors must inherit from strongly-typed sealed hierarchies (`DomainError`).

- **`:application` (Use Cases & Ports)**:
  - Contains application use cases, interactors, and port interfaces (`IAgyEnginePort`).
  - Depends only on `:domain`.
  - Zero UI, platform, or persistence framework details.
  - Exposes asynchronous state transitions via Kotlin `Flow` and coroutines.

- **`:infrastructure` (Adapters & Integrations)**:
  - Implements ports defined in `:application` and `:domain`.
  - Handles I/O, networking (Ktor), bridge communication, SSH, microVM/Docker processes, and OS-specific services.
  - Must never leak low-level transmission or protocol details to upper layers.

- **`:presentation` (MVI State & UI)**:
  - Compose Multiplatform UI, ViewModels, and unidirectional MVI state machines (`SessionViewState`).
  - Depends on `:application` and `:domain`.
  - Views observe immutable state; UI events trigger use case interactions.

---

## 2. Kotlin 2.1 & Coroutines Idioms

- **Immutability & Typing**:
  - Prefer immutable `val` properties and `data class` / `data object` declarations.
  - Utilize `@JvmInline value class` for strongly-typed identifiers (`SessionId`, `TargetAddress`).
  - Sealed interfaces for state hierarchies and connection events.
- **Coroutines & Concurrency**:
  - Adhere to structured concurrency; never use `GlobalScope`.
  - Prefer `SharedFlow` / `StateFlow` with backpressure-aware buffers for event streams.
  - Inject coroutine dispatchers for testability.

---

## 3. Code Quality & Static Analysis

- **Detekt**:
  - All code must pass `./gradlew detekt` cleanly without suppressed warnings or disabled rules.
  - Formatting adheres to `detekt-formatting` rules.
- **Zero Magic Literals**:
  - Define reusable timeout values, identifiers, and configuration keys as named constants.
- **Testing**:
  - Pure JVM unit test coverage across `:domain` and `:application` modules without Android dependencies.
  - Use JUnit Jupiter 5, MockK, and Turbine for Flow verification.
