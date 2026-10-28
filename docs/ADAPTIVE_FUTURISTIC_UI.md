# Adaptive Futuristic UI — Implementation and QA Plan

## Implemented in this branch

- The expanded-screen navigation rail now reads selected, unselected, and indicator colors from the active Material 3 color scheme instead of fixed dark-theme colors.
- This fixes contrast and visual consistency when users select the light theme or enable Android dynamic colors.
- The existing breakpoint keeps bottom navigation on compact screens and a navigation rail on screens at least 600 dp wide.

## Design direction

Pixel Booster uses a restrained futuristic style: deep surfaces, cool cyan/blue accents, rounded cards, clear hierarchy, and purposeful motion. Avoid excessive neon, glowing text, constant pulsing, or decorative gauges that make measurements look more precise than they are.

### Adaptive layout rules

- Compact width (< 600 dp): bottom navigation; single-column dashboard; cards fill available width.
- Expanded width (>= 600 dp): navigation rail; content remains centered and constrained rather than stretching indefinitely.
- Large text: all essential actions and metric labels must remain visible with Android font scaling enabled; no fixed-height text containers.
- Landscape: cards may use two columns only when the available width supports readable card content.
- Touch targets: aim for at least 48 dp for actionable controls.
- Insets: content and navigation must not overlap system bars, display cutouts, or gesture areas.
- Theme: support system, explicit dark, explicit light, and dynamic colors where Android supports them.

### Motion rules

- Use short, purposeful transitions for navigation and state changes.
- Keep motion non-essential: the interface must remain understandable when system animations are disabled.
- Do not animate telemetry values as if they were live when no fresh measurement is available.
- Loading indicators must have an accessible description and must not imply an optimization is running unless it actually is.

### Smart Boost UX requirements

- Explain each action before opening Android settings or clearing app-owned cache.
- Show before/after observations as measurements, not guaranteed gains.
- Never claim to increase physical RAM, cool the CPU through software, improve battery health, or optimize another app's private data.
- Label unavailable sensor data as unavailable; do not substitute guessed values.
- Keep user confirmation for destructive or external system actions.

## Regression checklist

### Navigation and layout
- [ ] Pixel 6 portrait: bottom navigation, no clipped dashboard cards.
- [ ] Compact device landscape: scrollable content and usable navigation.
- [ ] Tablet / emulator width >= 600 dp: navigation rail and constrained content.
- [ ] Switch among Home, Smart Boost, and More destinations; selection remains correct.
- [ ] Verify edge-to-edge layout and system-bar insets.

### Theme and accessibility
- [ ] System theme, explicit light, explicit dark, and dynamic colors.
- [ ] Selected and unselected navigation contrast remains readable in both themes.
- [ ] Font size at default and largest supported accessibility setting.
- [ ] TalkBack labels on icon-only actions and meaningful content descriptions for charts/gauges.
- [ ] Reduced-motion/system animation scale zero does not hide state changes.
- [ ] Interactive targets are comfortably touchable.

### Smart Boost correctness
- [ ] Canceling a guided action causes no destructive side effects.
- [ ] App cache cleanup affects only this application's cache/code-cache locations.
- [ ] Before/after readings are timestamped and never represented as guaranteed improvements.
- [ ] Permission denial and unavailable metrics produce explanatory states.
- [ ] Offline or failed network checks show unavailable results instead of fabricated values.

### Release gate
- [ ] Run `gradle :app:testDebugUnitTest --no-daemon`.
- [ ] Run `gradle :app:assembleDebug --no-daemon`.
- [ ] Install and manually validate on physical Pixel 6 using `docs/PIXEL6_MANUAL_VALIDATION.md`.
- [ ] Review accessibility and contrast on both light and dark themes.
- [ ] Do not call the app device-verified until the physical-device checklist is completed.
