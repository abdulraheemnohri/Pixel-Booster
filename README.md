# Pixel Booster Advanced — Complete Android Utility Suite

[![Android Build & Release CI](https://github.com/abdulraheem/pixelbooster/actions/workflows/android-build.yml/badge.svg)](https://github.com/abdulraheem/pixelbooster/actions/workflows/android-build.yml)
[![Code & Robolectric Testing](https://github.com/abdulraheem/pixelbooster/actions/workflows/code-test.yml/badge.svg)](https://github.com/abdulraheem/pixelbooster/actions/workflows/code-test.yml)
[![File Structure & Lint Validation](https://github.com/abdulraheem/pixelbooster/actions/workflows/file-validation.yml/badge.svg)](https://github.com/abdulraheem/pixelbooster/actions/workflows/file-validation.yml)

**Product:** Pixel Booster Advanced  
**Package ID:** `com.abdulraheem.pixelbooster`  
**Version:** 2.0.0  
**Target Platform:** Native Android (Optimized for Google Pixel & Android 8.0 - 16)  
**Min SDK:** 26 (Android 8.0) | **Target SDK:** 36 (Android 16)  
**Language:** Kotlin | **UI Framework:** Jetpack Compose & Material Design 3  
**Architecture:** MVVM, Clean Architecture, Kotlin Coroutines & StateFlow  
**Storage:** Room SQLite Database & Jetpack DataStore Preferences  

---

## Overview & Core Philosophy

Pixel Booster Advanced is a premier native Android hardware telemetry, diagnostic, and optimization dashboard designed specifically for Google Pixel and modern Android devices. It provides transparent hardware metrics, deterministic device health scoring, battery health analysis, storage cleanup, speed testing, and interactive hardware sensor validation without deceptive claims or root requirements.

1. **Genuine Hardware Telemetry:** Directly uses public Android platform APIs (`ActivityManager`, `StatFs`, `BatteryManager`, `PowerManager`, `SensorManager`).
2. **Zero Fictitious Metrics:** Never simulates freed RAM or manufactures artificial benchmarks.
3. **100% On-Device Privacy:** Zero telemetry egress. All logs and diagnostics reside locally in Room database.
4. **Pixel-Specific Ergonomics:** Immersive dark Cyberpunk / Material You design system with Space Grotesk typography.

---

## Feature Modules & Navigation

1. **Home Dashboard (`/dashboard`):** Live telemetry gauges for RAM headroom, storage capacity, thermal state, and quick actions.
2. **Booster Suite (`/booster`):** Cache cleaner, RAM headroom optimizer, and junk sweep tools.
3. **Startup Manager (`/startup`):** Review boot apps and background execution profiles.
4. **Health Diagnostics (`/health`):** Battery health grade, cycle count, temperature curves, and mitigation tips.
5. **Storage Analyzer (`/storage`):** Breakdown of internal storage usage, large files, and app caches.
6. **Speed Test (`/speedtest`):** Network latency, download and upload throughput checks.
7. **Usage Insights (`/usage`):** Daily screen time and foreground duration statistics.
8. **Privacy Auditor (`/privacy`):** App permission monitoring and tracker detection.
9. **History Logs (`/history`):** Persistent time-series charts and optimization audit logs.
10. **Hardware Tests (`/diagnostics`):** Interactive touch digitizer test, haptic actuator test, audio speaker test, and full sensor scan.
11. **Settings (`/settings`):** Theme mode, dynamic color, sampling rate sliders, notification thresholds, and report export.

---

## APK Artifacts

Pre-built release and debug APKs are automatically generated and stored in the `/apk/` directory and available via GitHub Actions:
- Debug APK: `apk/app-debug.apk`
- Release-ready Build: `apk/PixelBooster-v2.0.0-debug.apk`

---

## GitHub Workflows & CI/CD

- **`android-build.yml`**: Compiles debug and release APKs, uploading downloadable artifacts.
- **`code-test.yml`**: Runs JUnit unit tests and Robolectric test suite (`gradle :app:testDebugUnitTest`).
- **`file-validation.yml`**: Validates project structure and Gradle build tree.
- **`auto-merge.yml`**: Automated pull request and dependabot merging.

---

## Build Instructions

Run tests:
```bash
gradle :app:testDebugUnitTest
```

Assemble debug APK:
```bash
gradle :app:assembleDebug
```
