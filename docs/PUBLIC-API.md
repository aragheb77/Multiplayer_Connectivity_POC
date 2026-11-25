# Public API

Package: `com.ragheb.connectivity`

## Initialization

```kotlin
val client = ConnectivityClient(
    context = applicationContext,
    config = ConnectivityConfig(
        serviceId = "com.ragheb.localpoc.service",
        localDeviceName = "My Android device",
        maxConcurrentPeers = 4
    ),
    listener = listener
)
```

The host app must request the permissions merged from the library manifest before advertising/discovery. Listener callbacks are posted on the main thread. Invoke public methods from the main thread. Use the application context; call `shutdown()` when the owning component is permanently finished.

## Operations

| Method | Purpose |
|---|---|
| `startAdvertising()` / `stopAdvertising()` | Publish local endpoint presence |
| `startDiscovery()` / `stopDiscovery()` | Discover endpoints using the same `serviceId` |
| `requestConnection(deviceId)` | Initiate connection |
| `acceptConnection(deviceId)` | Accept after local authentication-code comparison |
| `rejectConnection(deviceId)` | Reject incoming request |
| `sendMessage(deviceId, text)` | Send UTF-8 text to an authenticated connected peer |
| `disconnect(deviceId)` / `disconnectAll()` | End peer sessions |
| `shutdown()` | Stop operations and release session resources |

## Listener events

`ConnectivityListener` exposes discovery state, device found/lost, incoming connection request (including verification code), peer state, authentication state, received/sent messages, and structured failures.

## Models

- `NearbyDevice(id, name)` — ephemeral endpoint ID and display name.
- `PeerConnectionRequest(device, verificationCode)` — incoming authentication prompt.
- `ChatMessage(peerId, text, sentByLocalDevice, timestampMillis)`.
- `ConnectivityFailure(code, message, cause)`.
- `DiscoveryState`, `PeerState`, and `AuthenticationState` — explicit lifecycle states.

## Authentication contract

A host app must show `verificationCode` and obtain explicit user approval through `acceptConnection` or `rejectConnection`. The same code is expected on both devices. The adapter only reports `AUTHENTICATED`/`CONNECTED` after the Nearby connection result succeeds following mutual acceptance. Discovery by itself is never treated as identity verification.

## Errors and limits

Failures are delivered through `onFailure`. Sending to a peer that is not both connected and authenticated is rejected. This POC sends BYTES payloads and keeps chat messages below the Nearby 32 KB BYTES limit. The host owns runtime permission UX and radio-settings guidance.
