# Local Multiplayer Connectivity POC (Android)

**Standalone project.** This repository is independent of Circles Triangle. It contains a reusable Android library and two independent sample applications. No source files from the existing game or its Wi-Fi/network implementation are included or modified.

## Current delivery status

- **Source project:** created.
- **Reusable library boundary:** created (`:connectivity`).
- **Transport adapter:** Nearby Connections implementation added; Google SDK types are confined to the adapter.
- **Sample consumer 1:** basic discovery, peer request/approval, manual code comparison, chat, and disconnect UI.
- **Sample consumer 2:** separate application ID and separate consumer implementation using only the library public API.
- **Build verification:** not run locally; Android SDK and Gradle are unavailable in the authoring environment. The included GitHub Actions workflow builds both debug APKs and uploads them as a 14-day artifact once pushed to a repository.
- **Physical-device verification:** not run. Requires two physical Android devices and Google Play services.
- **APK:** not included in this source archive. No successful build has yet produced an APK.

Do not treat this source delivery as a passed POC. See `docs/TEST-REPORT.md` for the actual verification state.

## Project structure

```text
LocalMultiplayerPOC/
├── connectivity/                 # reusable Android library
│   └── src/main/java/com/ragheb/connectivity/
│       ├── ConnectivityClient.kt # stable host-app facade
│       ├── ConnectivityModels.kt # transport-independent models/events
│       └── internal/NearbyConnectionsTransport.kt
├── sample-app/                   # first standalone test application
├── sample-app-two/               # independent second consumer application
├── docs/
│   ├── ARCHITECTURE.md
│   ├── PUBLIC-API.md
│   ├── INTEGRATION-GUIDE.md
│   ├── FEASIBILITY.md
│   ├── TEST-REPORT.md
│   └── NETWORK-PRESERVATION.md
└── .github/workflows/android-build.yml
```

## Open and build on Windows

1. Install Android Studio (current stable) and a JDK 17.
2. Extract this ZIP to a normal local folder (avoid a cloud-synced or read-only directory).
3. In Android Studio choose **Open**, then select the `LocalMultiplayerPOC` folder (the folder containing `settings.gradle.kts`).
4. Allow Android Studio to download the Android Gradle Plugin, Kotlin plugin, Android SDK platform 35, Build Tools, and Maven dependencies.
5. If Android Studio asks for a Gradle distribution, use Gradle **8.13**. The project uses Android Gradle Plugin **8.13.0** and JDK 17.
6. Select `sample-app` in the run-configuration/module selector and run it on a physical Android phone. Build the second app by selecting `sample-app-two`.
7. The two apps have different package IDs and can be installed side-by-side. Both use the same Nearby service ID so they can discover each other.

A Gradle wrapper JAR could not be generated in the authoring environment. For Windows, after Android Studio has created `local.properties` (or you have created it with your SDK path), run `build-windows.bat` from the project folder. It downloads Gradle 8.13 if needed and builds both APKs. A GitHub Actions workflow is also included for reproducible command-line builds after the source is placed in a repository.

## Two-device test setup

1. Install `sample-app` on device A and `sample-app-two` on device B (or install either app on both devices).
2. Ensure both devices have Google Play services and grant the nearby-device/Bluetooth permissions requested by Android.
3. Enable Wi-Fi and Bluetooth manually in Android Settings. The app does not toggle either radio. Internet access is not required for the peer session, but Google Play services must be available on the devices.
4. On both devices, press **Start advertising** and **Discover** (Consumer 2 has one combined button).
5. Wait for the other device to appear. Tap it to request a connection.
6. On each device, compare the displayed six-digit authentication code with the other device. Accept only if the codes match on both screens.
7. After both sides accept, select the peer and exchange messages in both directions.
8. Disconnect, repeat discovery, and test unexpected disconnect by disabling a radio or force-stopping one app.

For a strict offline test, first ensure both devices have Google Play services provisioned, then disable mobile data and disconnect from internet while leaving Wi-Fi and Bluetooth radios enabled. Do not turn Wi-Fi off for this test: Nearby Connections can use Wi-Fi radio locally without an internet connection.

## Dependency

`com.google.android.gms:play-services-nearby:19.5.1` (Google Play services release information current at the time this POC was authored). Both devices must have compatible Google Play services installed. Devices without Google Play services are not supported by this selected transport.

## Important POC limits

- This is an Android-only feasibility project, not a production SDK release.
- `P2P_CLUSTER` is selected to permit more than two peers; this sample UI is optimized for the two-device test.
- Authentication is the Nearby-generated short code shown on both devices. The user must manually compare it and approve on each device. A connected state is reported only after the Nearby handshake succeeds following mutual acceptance.
- The current message protocol is a small UTF-8 JSON envelope carried as a Nearby BYTES payload. Nearby BYTES payloads are limited to 32 KB.
- No automatic reconnection, durable identity, account system, background service, or game-state synchronization is included.
- Radio state checks are best-effort UI guidance. The underlying SDK/device combination may require additional system-specific handling discovered during physical-device tests.
- The code is a first implementation candidate and must pass compilation, device testing, lifecycle/error testing, and security review before production reuse.
