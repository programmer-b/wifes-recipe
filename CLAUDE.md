# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands
- Build: `./gradlew build`
- Run: `./gradlew installDebug`
- Run unit tests: `./gradlew test`
- Run specific unit test: `./gradlew :app:testDebugUnitTest --tests "com.dantech.wife.recipe.TestClassName.testMethodName"`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Run specific instrumented test: `./gradlew :app:connectedAndroidTest -PtestClass="com.dantech.wife.recipe.TestClassName"`
- Lint: `./gradlew lint`

## Style Guidelines
- Kotlin style: Standard Kotlin conventions with 4-space indentation
- Imports: Group by package, no wildcards, alphabetically sorted
- Naming: PascalCase for classes/composables, camelCase for variables/functions, snake_case for test methods
- Composables: Should be marked with @Composable, parameter default values where appropriate
- Error handling: Use Result or Exception handling for expected errors; prefer immutability
- Architecture: Follow Jetpack Compose best practices with stateless UI components
- Types: Prefer explicit types for public APIs, inference for local variables
- Preview: Include @Preview for all composable UI elements