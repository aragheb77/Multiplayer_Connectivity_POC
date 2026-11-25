# Feasibility assessment

## Preliminary conclusion

**CONDITIONALLY FEASIBLE for an Android-only POC.** Google Nearby Connections is designed for offline peer-to-peer discovery, connection negotiation, and bidirectional payload exchange. It supports a cluster strategy suitable for more than two devices. This is a suitable candidate, subject to physical-device validation and current radio/permission behavior.

## Verified from current official documentation

- Nearby Connections supports discovery, connection, and real-time exchange without internet access; it abstracts Bluetooth, BLE, and Wi-Fi transports. (Google overview: https://developers.google.com/nearby/connections/overview)
- Both sides receive a connection initiation and can accept or reject. The authentication digits are shared between both devices and can be compared by users. Google cautions against establishing unauthenticated connections. (Manage connections: https://developers.google.com/nearby/connections/android/manage-connections)
- BYTES payloads are limited to 32 KB; the POC uses them for short text messages. (Overview above)
- `P2P_CLUSTER` supports M-to-N topology. (Strategy reference: https://developers.google.com/android/reference/com/google/android/gms/nearby/connection/Strategy)
- Current Android permission requirements vary by OS version; Google documents Bluetooth and nearby Wi-Fi runtime permissions and notes that advertising/discovery will not start if required permissions are denied. (Get started: https://developers.google.com/nearby/connections/android/get-started)
- Google announced on 20 July 2026 that Nearby Connections will no longer automatically turn on Wi-Fi/Bluetooth for first- and third-party apps, with the change scheduled for late 2026. The app must detect disabled radios and guide users to enable them manually. (https://developer.android.com/blog/posts/upcoming-changes-to-the-nearby-connections-api)
- Current Google Play services setup documentation lists `play-services-nearby:19.5.1`; compatible Android devices with Google Play services are required. (https://developers.google.com/android/guides/setup)

## Selected POC direction

Use `com.google.android.gms:play-services-nearby:19.5.1`, `Strategy.P2P_CLUSTER`, byte payloads, manual comparison of Nearby authentication digits, and explicit manual radio enablement. The project targets Android API 35 and minimum API 24 for the initial POC. Verify target SDK and permission changes again before distribution.

## Dependencies and risks

- Google Play services must be installed and operational. Devices without Google Play services are outside this adapter's support scope.
- Actual radio combinations and OEM behavior need testing across physical devices.
- The API's late-2026 radio behavior is a material integration risk; this project never toggles radios and gives manual settings guidance in the primary tester app.
- Google Play services SDK analytics/diagnostics may collect performance and device/app metadata as described in the Nearby overview; users can control Usage & diagnostics in Google settings. Review privacy disclosures before production.
- The authentication code is a human verification step. It is not a persistent user identity or account authentication system.
- Lifecycle, process death, permission revocation, and radio-loss behavior require physical-device tests and further hardening.

## Alternatives

No alternative transport is implemented. If Google Play services dependency or target device coverage proves unacceptable, evaluate Wi-Fi Direct or a local-network socket transport in a separate decision. Do not switch without reviewing permissions, discovery UX, device compatibility, security, and approval impact.
