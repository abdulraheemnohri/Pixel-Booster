# Pixel Booster Advanced — Device Compatibility Matrix

Pixel Booster Advanced is optimized specifically for Google Pixel devices while gracefully supporting any compatible Android device running Android 8.0+ (API 26+).

## Google Pixel Hardware Matrix

| Device Series | Silicon / SoC | Thermal API | Battery Temp Telemetry | Adaptive Charging Support |
|---------------|--------------|-------------|------------------------|---------------------------|
| **Pixel 6 / 6 Pro / 6a** | Google Tensor (G1) | Yes (Android 12+) | Yes (`ACTION_BATTERY_CHANGED`) | Yes |
| **Pixel 7 / 7 Pro / 7a** | Google Tensor G2 | Yes (Android 13+) | Yes (`ACTION_BATTERY_CHANGED`) | Yes |
| **Pixel Fold / Tablet** | Google Tensor G2 | Yes (Android 13+) | Yes (`ACTION_BATTERY_CHANGED`) | Yes |
| **Pixel 8 / 8 Pro / 8a** | Google Tensor G3 | Yes (Android 14+) | Yes (`ACTION_BATTERY_CHANGED`) | Yes |
| **Pixel 9 / 9 Pro / XL / Fold** | Google Tensor G4 | Yes (Android 14+) | Yes (`ACTION_BATTERY_CHANGED`) | Yes |
| **Pixel 10 Series** | Google Tensor G5 | Yes (Android 15+) | Yes (`ACTION_BATTERY_CHANGED`) | Yes |
| **Pixel 4 / 4 XL / 4a / 5** | Qualcomm Snapdragon | Yes (Android 10+) | Yes (`ACTION_BATTERY_CHANGED`) | Yes |
| **Other Android Phones** | Snapdragon / Dimensity / Exynos | Yes (Android 10+) | Yes (Hardware dependent) | Device specific |

## API Guarding & Graceful Fallbacks

1. **PowerManager Thermal Status:**
   - Supported on Android 10+ (API 29+).
   - On older versions, the Thermal card clearly reflects `Unsupported` with guidance to monitor battery cell temperatures.
2. **App Usage Insights:**
   - Requires user activation via Android Settings (`Settings.ACTION_USAGE_ACCESS_SETTINGS`).
   - If denied or unsupported by enterprise work profiles, a clear educational state is shown with zero crashes.
3. **Internal Storage:**
   - Evaluated using Android's native `StatFs` on `Environment.getDataDirectory()`.
   - Handles multi-volume storage configurations cleanly.
4. **No-Root Philosophy:**
   - Never requests root or executes hidden non-SDK interfaces.
   - Delegates directly to native Android settings for battery and storage optimizations.
