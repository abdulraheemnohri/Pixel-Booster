# Pixel Booster Advanced — Complete Android Utility Suite

[![Android Build & Release CI](https://github.com/abdulraheemnohri/Pixel-Booster/actions/workflows/android-build.yml/badge.svg)](https://github.com/abdulraheemnohri/Pixel-Booster/actions/workflows/android-build.yml)
[![Signed APK Release](https://github.com/abdulraheemnohri/Pixel-Booster/actions/workflows/release-apk.yml/badge.svg)](https://github.com/abdulraheemnohri/Pixel-Booster/actions/workflows/release-apk.yml)
[![Code & Robolectric Testing](https://github.com/abdulraheemnohri/Pixel-Booster/actions/workflows/code-test.yml/badge.svg)](https://github.com/abdulraheemnohri/Pixel-Booster/actions/workflows/code-test.yml)

**Product:** Pixel Booster Advanced  
**Package ID:** `com.abdulraheem.pixelbooster`  
**Version:** 2.0.0  
**Target Platform:** Native Android (Google Pixel and Android 8.0+)  
**Min SDK:** 26 | **Target SDK:** 36  
**Language:** Kotlin | **UI Framework:** Jetpack Compose & Material Design 3  
**Architecture:** MVVM, Kotlin Coroutines  
**Storage:** Room SQLite Database & Jetpack DataStore Preferences

---

## Overview & Core Philosophy

Pixel Booster is an Android device diagnostics and optimization guide designed for Pixel and other compatible Android devices. It should use public Android APIs, explain limitations clearly, and avoid claims that an app can magically increase physical RAM or force-stop other apps. Some cleanup actions must be completed by the user in Android system settings.

- **Transparent telemetry:** Use supported Android platform APIs and clearly label estimates.
- **No fictitious metrics:** Never invent freed RAM, battery health, or benchmark results.
- **Privacy:** Keep diagnostic data on-device unless a feature clearly explains otherwise.
- **Pixel-focused UX:** Dark Material 3 design with dynamic color where supported.

## Feature modules

1. **Dashboard:** Device and storage overview.
2. **Smart Boost:** Guided steps for users to review background apps and Android settings.
3. **Health diagnostics:** Battery and thermal information where the device exposes it.
4. **Storage analyzer:** Storage overview and routes to supported cleanup controls.
5. **Network tests:** Clearly label test methodology and network usage.
6. **Usage insights:** Requires user-granted Usage Access where applicable.
7. **Privacy auditor:** Review permission information available to the app.
8. **History:** Local diagnostic and action logs.
9. **Hardware tests:** Interactive tests for supported sensors and hardware.
10. **Settings:** Theme, sampling options, notifications, and report export.

Feature availability depends on Android version, device manufacturer restrictions, and granted permissions.

---

## Download a release APK

The signed APK is published as a downloadable asset on the GitHub Releases page:

**[Open Pixel Booster Releases](https://github.com/abdulraheemnohri/Pixel-Booster/releases)**

The release workflow attaches:
- `PixelBooster-vX.Y.Z.apk` — signed installable APK.
- `PixelBooster-vX.Y.Z.apk.sha256` — SHA-256 checksum file.

APKs are **not** committed into the repository's `/apk/` folder. Download the APK from a release's **Assets** section. GitHub Actions build artifacts are temporary workflow artifacts and are different from permanent GitHub Release assets.

## Maintainers: configure secure signing

The workflow `.github/workflows/release-apk.yml` builds and verifies a signed release APK, then attaches it to a GitHub Release. Configure these repository Actions secrets before running it:

- `KEYSTORE_BASE64`: Base64-encoded bytes of the private upload keystore.
- `KEYSTORE_PASSWORD`: The keystore password.
- `KEY_PASSWORD`: The password for the `upload` alias.

The alias is currently fixed as `upload` in `app/build.gradle.kts`. Do not commit the keystore, passwords, or Base64 value to Git. Keep an encrypted backup of the keystore and passwords.

### Create a keystore in Linux / Debian

Install Java's keytool if needed:

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
```

Generate the keystore (you will be prompted for passwords):

```bash
mkdir -p "$HOME/.android/keystores"
chmod 700 "$HOME/.android/keystores"
keytool -genkeypair -v \
  -keystore "$HOME/.android/keystores/pixel-booster-upload.jks" \
  -alias upload -keyalg RSA -keysize 2048 -validity 10000
chmod 600 "$HOME/.android/keystores/pixel-booster-upload.jks"
```

Validate it:

```bash
keytool -list -v \
  -keystore "$HOME/.android/keystores/pixel-booster-upload.jks" \
  -alias upload
```

Copy the Base64 output from this command into the `KEYSTORE_BASE64` secret. The output is sensitive; never print it in public logs or commit it:

```bash
base64 -w 0 "$HOME/.android/keystores/pixel-booster-upload.jks"
```

If `base64 -w 0` is unsupported, use:

```bash
base64 "$HOME/.android/keystores/pixel-booster-upload.jks" | tr -d '\n'
```

Add secrets at [Settings → Secrets and variables → Actions](https://github.com/abdulraheemnohri/Pixel-Booster/settings/secrets/actions).

### Publish a release

Option A — from the GitHub website:
1. Open **Actions → Release Signed APK**.
2. Select **Run workflow**.
3. Enter a release tag that matches the app's configured version, for example `v2.0.0`.
4. Run the workflow and wait for the signed APK verification step to pass.
5. Open **Releases** and download the APK under **Assets**.

Option B — push a version tag from a clone:

```bash
git tag v2.0.0
git push origin v2.0.0
```

Use a new version tag for each new app version. Update `versionName` and `versionCode` in `app/build.gradle.kts` before releasing a newer app version. Never reuse an existing version tag for a different binary.

## Build and test locally

From the repository root, if the project has a Gradle wrapper:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

If no `gradlew` wrapper is present, use a compatible Gradle installation or generate and commit the Gradle wrapper for reproducible builds. The debug APK is for testing; use the signed release workflow for public distribution.

## GitHub workflows

- `.github/workflows/android-build.yml`: Debug build and optional release build artifacts.
- `.github/workflows/release-apk.yml`: Signed release APK, signature verification, checksum, and GitHub Release upload.
- `.github/workflows/code-test.yml`: Unit tests.
- Other workflow files may provide additional repository validation or automation.

