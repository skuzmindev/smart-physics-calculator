# Physics Calculator

An Android app that solves common physics formulas for any of their variables, shows the formula
and an explanation alongside each calculation, and keeps a history of saved results.

Originally a 2020 Samsung Android Development capstone project — a Java app with a hardcoded,
Russian-only UI. Rebuilt in 2026 as a Kotlin / Jetpack Compose app.

## Features

- **14 formulas, 45 rearrangements.** Pick any quantity as the unknown; the app solves for it.
- **Live results.** Values update as you type — no calculate button.
- **Explanations.** Every formula shows its canonical form, what it means, and when it applies.
- **Search** across formula titles, quantity names, and symbols.
- **History.** Saved calculations persist locally and reopen prefilled.
- Material 3 with dynamic color and dark theme.

Topics: uniform motion, uniformly accelerated motion, Newton's second law, weight, sliding
friction, Archimedes' principle, Hooke's law, kinetic energy, gravitational potential energy,
momentum, work, power, pressure, and Ohm's law.

## Build

Requires JDK 17+ (Android Studio's bundled JBR works) and the Android SDK.

```bash
./gradlew assembleDebug
```

Install to a connected device or emulator:

```bash
./gradlew installDebug
```

Run the unit tests:

```bash
./gradlew testDebugUnitTest
```

## Architecture

MVVM, single activity, no dependency-injection framework — the app is small enough not to need one.

```
domain/     Formula.kt    Quantity, Solver, Formula types plus number parsing and formatting
            Formulas.kt   The catalog: every formula and its rearrangements, as data
data/       HistoryRepository.kt   DataStore-backed calculation history
ui/         PhysicsCalcApp.kt      Navigation Compose graph with type-safe routes
            FormulaListScreen.kt   Searchable list
            CalculatorScreen.kt    One screen that renders all 45 calculators
            CalculatorViewModel.kt Input state, validation, live results
            HistoryScreen.kt       Saved calculations
```

The design decision that shapes everything: **calculators are data, not code.** A formula is a
title, an expression, an explanation, and a list of solvers — each solver being a target quantity,
its inputs, and a one-line lambda. Adding a topic means adding a row to `Formulas.kt`; no new
screen, layout, or ViewModel. The original version had one fragment, one layout, and one empty
ViewModel per calculation, which is why 2,101 lines of Java became roughly 400 lines of Kotlin
while gaining features.

`domain/` has no Android imports, so the physics is covered by fast JVM unit tests. The main test
asserts that every solver reproduces its own target from a physically consistent set of values —
one loop that fails if any of the 45 rearrangements is wrong.

## Stack

Kotlin 2.4 · AGP 9.3 · Gradle 9.5 · Compose BOM 2026.08 · Material 3 · Navigation Compose ·
DataStore · kotlinx.serialization · minSdk 24 · compileSdk 37
