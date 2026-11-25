package com.ragheb.connectivity

/** Stable, transport-independent public models. */
data class NearbyDevice(val id: String, val name: String)
data class PeerConnectionRequest(val device: NearbyDevice, val verificationCode: String)
data class ChatMessage(val peerId: String, val text: String, val sentByLocalDevice: Boolean, val timestampMillis: Long)
data class ConnectivityFailure(val code: String, val message: String, val cause: Throwable? = null)

enum class DiscoveryState { IDLE, STARTING, ACTIVE, STOPPING, FAILED }
enum class PeerState { REQUESTING, AWAITING_USER_VERIFICATION, CONNECTED, DISCONNECTED, REJECTED, FAILED }
enum class AuthenticationState { NOT_STARTED, AWAITING_VERIFICATION, AUTHENTICATED, REJECTED, FAILED }

interface ConnectivityListener {
    fun onDiscoveryStateChanged(state: DiscoveryState) {}
    fun onDeviceFound(device: NearbyDevice) {}
    fun onDeviceLost(deviceId: String) {}
    fun onConnectionRequest(request: PeerConnectionRequest) {}
    fun onPeerStateChanged(device: NearbyDevice, state: PeerState) {}
    fun onAuthenticationStateChanged(device: NearbyDevice, state: AuthenticationState) {}
    fun onMessage(message: ChatMessage) {}
    fun onFailure(error: ConnectivityFailure) {}
}

data class ConnectivityConfig(
    val serviceId: String,
    val localDeviceName: String,
    val maxConcurrentPeers: Int = 4
) {
    init { require(serviceId.isNotBlank()); require(localDeviceName.isNotBlank()); require(maxConcurrentPeers >= 1) }
}
