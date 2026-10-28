# Privacy Policy — Pixel Booster 2.0

Pixel Booster is designed to keep diagnostic history and preferences on the device. The app does request network access for its explicit connectivity/latency tools; therefore, it is **not accurate to describe the app as having zero network access**.

## 1. Network use

- The Android manifest declares `android.permission.INTERNET` and network-state permissions for connectivity checks.
- The network tools may attempt a connection to Google Public DNS (`8.8.8.8:53`) or an HTTPS request to `www.google.com` to measure connectivity/latency.
- This repository's app code should not be described as fully offline while these tools are enabled.
- The privacy policy does not claim that network activity is impossible. Review the network tool implementation before each release and keep destinations and purposes disclosed.

## 2. Analytics, tracking, and advertising

- The project is intended to contain no advertising SDK or analytics-based monetization.
- Verify the resolved dependency tree and built APK before making absolute claims about third-party SDKs.
- The app does not require a user account or sign-in for its core features.

## 3. Local data storage

- Diagnostic samples are stored in the local Room database.
- App preferences are stored with Jetpack DataStore.
- Event and optimization history may be stored locally by the app.
- Users should be able to clear locally stored diagnostic history and event logs from the app's settings. Confirm each deletion action works before release.

## 4. App Usage Access

- Usage insights may use Android's `UsageStatsManager` after the user grants Usage Access in system settings.
- Usage statistics are sensitive device data. Explain why access is requested and avoid collecting more than the feature needs.

## 5. User-initiated report sharing

- If the user selects the diagnostic report export action, the app may invoke Android's system share sheet.
- Once the user selects another app, that app receives the shared content under Android's share flow and its own privacy policy.

## 6. Permissions and release checks

- Review every manifest permission and remove permissions not needed by implemented features.
- Do not claim that system-wide RAM, other apps' caches, CPU temperature drops, or network improvements are guaranteed. Android restricts what an ordinary no-root app can control.
- Recheck this policy against the exact source and packaged APK before publishing a release.
