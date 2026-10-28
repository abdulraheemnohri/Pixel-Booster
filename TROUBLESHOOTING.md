# Troubleshooting Guide — Pixel Booster Advanced

## 1. Why doesn't Pixel Booster "Clean RAM" with an animation?
Modern Android is built on Linux and uses aggressive low-memory management. Keeping frequently used applications in RAM allows fast task switching and conserves battery by avoiding CPU-intensive restarts from cold storage. Killing background processes causes Android's Zygote subsystem to immediately relaunch essential daemons, wasting battery power and CPU cycles. Pixel Booster accurately reports available RAM headroom without destructive gimmicks.

## 2. Why is Battery Cell Temperature showing "Unavailable"?
Certain Android OEM kernels or emulator environments do not broadcast temperature telemetry in `Intent.ACTION_BATTERY_CHANGED`. On physical Google Pixel devices, the battery controller provides live centigrade temperature with 0.1°C precision.

## 3. Why does App Usage say "Usage Access Required"?
To protect user privacy, Android requires explicit user opt-in before an app can read foreground durations. Tap "Enable Usage Access in Settings", locate "Pixel Booster", and toggle the permission.

## 4. How should I safely cool down my Google Pixel if hot?
1. **Natural Airflow:** Place the phone on a flat surface in a cool, shaded room.
2. **Remove Heavy Cases:** Thick protective cases trap heat dissipated through the Pixel's aluminum and glass chassis.
3. **Avoid Refrigerators & Ice:** Never place a hot electronic device in a freezer or on ice packs. Rapid temperature changes create internal humidity condensation that can trigger liquid contact indicators and damage motherboard components.
4. **Pause Fast Charging:** Fast charging generates ambient chemical heat. Disconnect high-wattage chargers until the cell cools below 38°C.
