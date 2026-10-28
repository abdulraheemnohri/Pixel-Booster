# Privacy Policy — Pixel Booster Advanced 2.0

Pixel Booster Advanced is built on a zero-egress, privacy-first architecture.

## 1. Zero Network Access
- The application does not declare the `android.permission.INTERNET` permission in `AndroidManifest.xml`.
- It has no network socket access, cannot make HTTP/HTTPS network calls, and cannot transmit telemetry off your physical device.

## 2. No Analytics, Tracking, or Advertising
- No Google Analytics, Firebase, Crashlytics, third-party trackers, or advertising SDKs are packaged in this application.
- No user accounts, registration, email collection, or sign-in workflows exist.

## 3. Local-First Data Storage
- Historical diagnostic telemetry samples are stored exclusively inside a local Room SQLite database on your device (`pixel_booster_diagnostics.db`).
- Application preferences are stored in Jetpack DataStore (`pixel_booster_settings.preferences_pb`).
- You have complete control to prune or erase diagnostic history and event logs at any time from the Settings screen.

## 4. App Usage Access
- App Usage statistics are queried using Android's native `UsageStatsManager` strictly on-device to calculate foreground hours and percentages.
- Usage duration information is never aggregated into persistent historical tables and never leaves device volatile memory.

## 5. User-Initiated Telemetry Export
- Telemetry reports are only shared when you deliberately click "Export Diagnostic Telemetry Report".
- This action invokes Android's system share sheet (`Intent.ACTION_SEND`), giving you complete control over which application receives the text file.
