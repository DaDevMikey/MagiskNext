# App Development Guide

This guide covers everything you need to know about modifying the Magisk Next application.

## Codebase Structure
All application-related source code lives under the `app` directory. When working on the application codebase, you should use `app` as your primary working directory.

- The Magisk Next app is written in **Kotlin** and **Java**. Prefer Kotlin for all new code.
- Modern UI components are built using **Jetpack Compose** (e.g., `ModuleScreen.kt`, `ToolboxScreen.kt`).
- The application uses **Kotlin Coroutines** and **Flows** for state management and background processing.

## Building the App
The `app` directory is a Gradle project. You can build the application using the included Gradle wrapper:

```sh
# Navigate to the app directory
cd app

# Build the release APK
./gradlew assembleRelease
```
After making changes in `app`, make sure to build the relevant modules to ensure they compile successfully.

## Interacting with the Core
The core daemon and native code interact with the application through various shell commands and file configurations:
- **Modules** are managed via `/data/adb/modules`. The app parses `module.prop` and scans for features like `webroot/` or `action.sh` dynamically.
- **Config Properties** (like bootloop protection state) are often read from or written to internal databases or properties.

When contributing to the Magisk Next app, ensure your UI changes seamlessly integrate with the existing Jetpack Compose architecture and that your coroutines properly switch context (e.g., `Dispatchers.IO` for shell commands).
