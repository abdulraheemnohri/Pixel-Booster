# Pixel Booster — Pixel 6 Manual Validation Checklist

This checklist supplements automated Android build and Robolectric tests. It is not evidence that a physical Pixel 6 has already been tested.

## Before testing
- [ ] Install the debug APK on a Google Pixel 6 running the intended Android version.
- [ ] Record Android version/build number, app version, and whether Usage Access has been granted.
- [ ] Confirm the app launches without crashing and navigation works across Home, Booster, Storage, Usage, Diagnostics, and More.
- [ ] Verify unavailable readings are labelled unavailable, not replaced with plausible-looking sample values.

## Device telemetry
- [ ] Compare available RAM and storage readings with Android system settings. Small timing differences are expected.
- [ ] Verify missing battery temperature/thermal data shows unavailable and is not rendered as a 0°C change.
- [ ] Verify the app does not claim it can cool the CPU, unlock CPU frequency, increase physical RAM, or kill other apps.
- [ ] Check battery and thermal screens with permissions denied and granted where applicable.

## App-owned cache cleanup
- [ ] Note app cache size before cleanup.
- [ ] Create a harmless test file under the app's own cache directory in a debug/test build and verify the cleanup reports bytes actually removed.
- [ ] Confirm app files, downloads, photos, shared media, and other apps' data are not targeted.
- [ ] Verify cleanup succeeds safely when cache directories are empty or unavailable.
- [ ] Confirm the result does not promise faster performance.

## Network measurement
- [ ] Test on working Wi-Fi, mobile data, and offline mode.
- [ ] Confirm each connection attempt is displayed independently.
- [ ] If one attempt fails, confirm that measurement and any two-sample difference are labelled unavailable.
- [ ] Confirm the UI does not invent a default ping or claim the connection was accelerated.
- [ ] Verify no system DNS, sockets, VPN, or private network settings are changed.

## Settings, privacy, and resilience
- [ ] Check each settings toggle's description and confirm it does not imply an unimplemented background service.
- [ ] Revoke optional permissions and confirm the app remains usable with clear guidance.
- [ ] Confirm diagnostic history remains local and can be cleared by the user.
- [ ] Exercise empty history, empty cache, unavailable sensors, no network, and low-storage states.
- [ ] Rotate the device and background/restore the app; check for crashes or lost critical UI state.
- [ ] Review logs for crashes and unexpected sensitive data.

## Release gate
- [ ] GitHub Android Debug Build CI passes on the final commit.
- [ ] GitHub Code & Robolectric Testing passes on the final commit.
- [ ] Manual Pixel 6 checklist results are recorded by a human tester before calling the release device-verified.
