# Local Multiplayer Connectivity POC (Android)

**Standalone project.** This repository is independent of Circles Triangle. It contains a reusable Android library and two independent sample applications. No source files from the existing game or its Wi-Fi/network implementation are included or modified.

## Current delivery status

**POC DECISION: REJECTED — do not integrate or reuse this implementation.** The owner has decided to stop this attempt and restart from scratch later.

- Source and initial reusable-library/sample structure exist, but are not accepted.
- CI did not produce a successful build of both sample apps. The latest workflow run failed during Gradle script compilation after the Kotlin toolchain change.
- No successful APK build has been verified.
- No two-device physical test, end-to-end connectivity test, or production-readiness review has been completed.
- The implementation is retained only as historical reference. Do not treat it as an approved baseline or continue implementation under this POC.

The decision is recorded in `docs/DECISION-RECORD.md`. Restart later as a new POC with fresh architecture/toolchain validation and acceptance gates.

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
