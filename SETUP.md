# Sevam Partner Setup

## Requirements

- Android Studio stable
- Android SDK API 35
- Java 21 or Android Studio bundled JBR

## Local File

Create `local.properties` locally if needed:

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
```

Do not commit `local.properties`.

## Commands

```powershell
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:installDebug
```

## Development Login

Use **Continue in Debug** while worker backend auth is not connected.

## Current Milestone

The app is mock-backed. It is intended for validating the worker onboarding, jobs, earnings, and profile experience before connecting real APIs.
