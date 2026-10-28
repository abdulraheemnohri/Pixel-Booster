# Pixel Booster

A native Android utility app focused on helping users understand their device status and find Android's supported settings for storage, battery, permissions, and app management.

**App name:** Pixel Booster  
**Package:** `com.abdulraheem.pixelbooster`  
**Current version:** `2.0.0`  
**Language:** Kotlin  
**UI:** Jetpack Compose / Material Design 3  
**Minimum Android version:** Android 8.0 (API 26)  
**Target SDK:** API 36

[![Android Debug Build](https://github.com/abdulraheemnohri/Pixel-Booster/actions/workflows/android-build.yml/badge.svg)](https://github.com/abdulraheemnohri/Pixel-Booster/actions/workflows/android-build.yml)
[![Unit Tests](https://github.com/abdulraheemnohri/Pixel-Booster/actions/workflows/code-test.yml/badge.svg)](https://github.com/abdulraheemnohri/Pixel-Booster/actions/workflows/code-test.yml)

## What it is

Pixel Booster is designed to provide a clear device overview and guide users to actions that Android allows ordinary, non-root apps to perform. It does not promise to increase physical RAM, secretly kill other apps, or bypass Android security restrictions.

## Features

- **Device dashboard:** A central place for supported device and system information.
- **Smart Boost guidance:** Helps users review their apps and navigate to relevant Android settings.
- **Storage overview:** Shows available storage information and points users to system cleanup options.
- **Battery and thermal information:** Displays information only when exposed by the device and Android version.
- **Network tools:** Provides network checks with clear labels about what is measured.
- **Usage insights:** Some insights require the user to grant Usage Access in Android settings.
- **Permission review:** Helps users review permissions visible to the app.
- **Local history:** Keeps supported diagnostic history on-device.
- **Hardware checks:** Offers checks for supported sensors and hardware capabilities.
- **Settings and reports:** App preferences and supported diagnostic report options.

Feature availability depends on Android version, device support, and permissions granted by the user. Android may restrict or hide some system metrics and actions.

## Privacy and safety principles

- No root access is required.
- Do not treat estimates as exact hardware measurements.
- Do not claim that clearing cache or closing apps permanently increases RAM or battery health.
- Ask for permissions only when a feature needs them and explain why.
- Keep diagnostic history on-device unless a feature clearly states otherwise.
- User-initiated system actions should open Android's own settings where direct control is restricted.

## Build and test

### Requirements

- JDK 17
- Android SDK Platform 36 and Android Build Tools 36.0.0
- Gradle 9.3.1 (the CI workflows use this version)

### Build a debug version

Run these commands from the repository root on a machine with the Android SDK installed:

```bash
gradle :app:assembleDebug --no-daemon
```

The debug APK is normally written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Run unit tests

```bash
gradle :app:testDebugUnitTest --no-daemon
```

This repository currently uses a system-installed Gradle in its CI jobs. If you build locally, use a compatible Gradle installation. A Gradle wrapper is not currently included in the repository.

## Automated checks

The repository keeps these GitHub Actions workflows:

| Workflow | Purpose |
|---|---|
| **Android Debug Build CI** | Builds the debug app on pushes, pull requests, and manual runs; stores the debug APK as a temporary workflow artifact. |
| **Code & Robolectric Testing** | Runs the app's unit-test task on pushes, pull requests, and manual runs. |

Workflow artifacts are temporary CI outputs, not source files. Check the Actions tab for each run's logs and result.

## Project identity

- **Application ID:** `com.abdulraheem.pixelbooster`
- **App label:** Pixel Booster
- **Version name:** `2.0.0`

Keep the application ID stable once the app is installed by users; changing it makes Android treat the app as a different application. Update the version name and version code together when preparing a new app version.

## Contributing

1. Create a branch for your change.
2. Keep changes focused and explain user-visible behavior.
3. Run the unit tests and debug build when possible.
4. Open a pull request with a short summary and test results.

## Disclaimer

Pixel Booster is a device-information and guided-settings utility. It cannot override Android's protected system controls, guarantee longer battery life, or increase the device's physical RAM. Always review system actions before confirming them.
