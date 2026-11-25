# Multiplayer Connectivity POC

Standalone Android feasibility POC for a reusable local multiplayer connectivity component using Google Nearby Connections.

## Scope

- Independent Android library module (`connectivity`).
- Two separate consumer/sample applications.
- Nearby Connections transport behind a transport-independent public API.
- Manual authentication-code comparison and message exchange.
- No integration with Circles Triangle or changes to its existing networking implementation.

## Status

The reviewed source package is available from the project conversation. This repository is being initialized; the complete source tree and CI-generated APK artifacts are not yet uploaded. Do not treat this repository as a build-verified or device-verified release.

Build prerequisites: Android Studio, JDK 17, Android SDK platform 35, Gradle 8.13, and Google Play services on test devices.

See the reviewed source archive and review report supplied with the project for the full implementation and validation status.
