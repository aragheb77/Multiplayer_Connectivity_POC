# Integration guide

1. Add the `connectivity` Android library module to the host project, or publish it to an internal Maven repository when release packaging is approved.
2. Add `implementation(project(":connectivity"))` to the host app module.
3. Use JDK 17 and ensure the host project can resolve Google Maven and Maven Central.
4. Merge the permissions declared by `connectivity/src/main/AndroidManifest.xml`; do not remove them. Request runtime permissions based on OS version:
   - Android 12+ (API 31+): `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`.
   - Android 13+ (API 33+): `NEARBY_WIFI_DEVICES`.
   - Android 10–12L: `ACCESS_FINE_LOCATION` (and location services may need to be enabled on some devices).
   - Android 9 and lower: `ACCESS_COARSE_LOCATION`.
5. Explain why nearby-device permissions are required. If denied, do not start advertising/discovery; show a permission rationale and allow retry through Android settings where appropriate.
6. Manually ask the user to enable Wi-Fi and Bluetooth if disabled. Do not attempt to toggle either radio programmatically.
7. Create one `ConnectivityClient` using the application context, a shared service ID, local device display name, and a `ConnectivityListener`.
8. Call `startAdvertising()` and `startDiscovery()` on the devices that should be mutually discoverable.
9. On `onDeviceFound`, show the device and call `requestConnection(device.id)` only after user selection.
10. On `onConnectionRequest`, display `verificationCode`; users must compare the value on both devices. Call `acceptConnection` only after local confirmation, otherwise call `rejectConnection`.
11. Enable messaging only after `onAuthenticationStateChanged(AUTHENTICATED)` and `PeerState.CONNECTED`.
12. Call `sendMessage(peerId, text)`; handle `onFailure` and display `onMessage` events.
13. Call `disconnect(peerId)` when leaving a session; call `shutdown()` when the component owner is permanently destroyed.

## Troubleshooting

- No peers: verify identical service ID and strategy, both devices advertising/discovering, permissions granted, radios enabled, devices nearby, Google Play services present/up to date.
- Permission failure: inspect runtime permission results and Android version-specific permissions.
- Connection rejected: verify both users accepted and compared identical authentication digits.
- Google Play services unavailable: selected transport cannot operate on that device; do not silently fall back to another transport.
- Unexpected disconnect: listen for `PeerState.DISCONNECTED`, update host UI, and let the user restart discovery.

The host application remains responsible for lifecycle policy (for example, stopping discovery when its screen is no longer active). This POC does not implement a foreground service or background discovery.
