package com.ragheb.connectivity.internal

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.Payload
import com.ragheb.connectivity.*
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

/** Isolated implementation detail: all Google Nearby SDK references stay in this class. */
internal class NearbyConnectionsTransport(
    context: Context,
    private val config: ConnectivityConfig,
    private val listener: ConnectivityListener
) : TransportAdapter {
    private val client = Nearby.getConnectionsClient(context)
    private val main = Handler(Looper.getMainLooper())
    private val devices = ConcurrentHashMap<String, NearbyDevice>()
    private val pendingAuth = ConcurrentHashMap<String, String>()
    private val authenticated = ConcurrentHashMap.newKeySet<String>()
    private val connected = ConcurrentHashMap.newKeySet<String>()
    private var advertising = false
    private var discovering = false
    private var closed = false

    private fun emit(block: () -> Unit) { main.post { if (!closed) block() } }
    private fun fail(code: String, message: String, cause: Throwable? = null) = emit { listener.onFailure(ConnectivityFailure(code, message, cause)) }
    private fun device(id: String, name: String? = null) = devices.getOrPut(id) { NearbyDevice(id, name ?: "Nearby device") }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (!authenticated.contains(endpointId) || payload.type != Payload.Type.BYTES) return
            val bytes = payload.asBytes() ?: return
            try {
                val json = JSONObject(String(bytes, StandardCharsets.UTF_8))
                if (json.optString("type") != "chat") return
                emit { listener.onMessage(ChatMessage(endpointId, json.optString("text"), false, json.optLong("time", System.currentTimeMillis()))) }
            } catch (t: Throwable) { fail("INVALID_PAYLOAD", "Received message could not be decoded.", t) }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    private val lifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            val d = device(endpointId, info.endpointName)
            pendingAuth[endpointId] = info.authenticationDigits
            emit {
                listener.onPeerStateChanged(d, PeerState.AWAITING_USER_VERIFICATION)
                listener.onAuthenticationStateChanged(d, AuthenticationState.AWAITING_VERIFICATION)
                listener.onConnectionRequest(PeerConnectionRequest(d, info.authenticationDigits))
            }
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            val d = device(endpointId)
            if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK && pendingAuth.containsKey(endpointId)) {
                connected.add(endpointId)
                authenticated.add(endpointId)
                emit {
                    listener.onAuthenticationStateChanged(d, AuthenticationState.AUTHENTICATED)
                    listener.onPeerStateChanged(d, PeerState.CONNECTED)
                }
            } else {
                connected.remove(endpointId); authenticated.remove(endpointId)
                emit {
                    listener.onAuthenticationStateChanged(d, AuthenticationState.FAILED)
                    listener.onPeerStateChanged(d, PeerState.FAILED)
                }
                fail("CONNECTION_FAILED", "Connection failed or was not mutually accepted (status ${result.status.statusCode}).")
            }
            pendingAuth.remove(endpointId)
        }
        override fun onDisconnected(endpointId: String) {
            connected.remove(endpointId); authenticated.remove(endpointId); pendingAuth.remove(endpointId)
            val d = device(endpointId)
            emit {
                listener.onPeerStateChanged(d, PeerState.DISCONNECTED)
                listener.onAuthenticationStateChanged(d, AuthenticationState.NOT_STARTED)
            }
        }
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val d = device(endpointId, info.endpointName)
            emit { listener.onDeviceFound(d) }
        }
        override fun onEndpointLost(endpointId: String) {
            devices.remove(endpointId)
            emit { listener.onDeviceLost(endpointId) }
        }
    }

    override fun startAdvertising() {
        if (closed || advertising) return
        client.startAdvertising(config.localDeviceName, config.serviceId, lifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
            .addOnSuccessListener { advertising = true }
            .addOnFailureListener { fail("ADVERTISING_START_FAILED", it.localizedMessage ?: "Could not start advertising.", it) }
    }
    override fun stopAdvertising() {
        if (!advertising) return
        client.stopAdvertising().addOnCompleteListener { advertising = false }
    }
    override fun startDiscovery() {
        if (closed || discovering) return
        emit { listener.onDiscoveryStateChanged(DiscoveryState.STARTING) }
        client.startDiscovery(config.serviceId, discoveryCallback,
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build())
            .addOnSuccessListener { discovering = true; emit { listener.onDiscoveryStateChanged(DiscoveryState.ACTIVE) } }
            .addOnFailureListener { emit { listener.onDiscoveryStateChanged(DiscoveryState.FAILED) }; fail("DISCOVERY_START_FAILED", it.localizedMessage ?: "Could not start discovery.", it) }
    }
    override fun stopDiscovery() {
        if (!discovering) return
        emit { listener.onDiscoveryStateChanged(DiscoveryState.STOPPING) }
        client.stopDiscovery().addOnCompleteListener { discovering = false; emit { listener.onDiscoveryStateChanged(DiscoveryState.IDLE) } }
    }
    override fun requestConnection(deviceId: String) {
        val d = devices[deviceId] ?: return fail("UNKNOWN_DEVICE", "The selected device is no longer available.")
        if (connected.size >= config.maxConcurrentPeers) return fail("PEER_LIMIT", "Maximum connected peers reached.")
        emit { listener.onPeerStateChanged(d, PeerState.REQUESTING) }
        client.requestConnection(config.localDeviceName, deviceId, lifecycleCallback)
            .addOnFailureListener { emit { listener.onPeerStateChanged(d, PeerState.FAILED) }; fail("REQUEST_FAILED", it.localizedMessage ?: "Connection request failed.", it) }
    }
    override fun acceptConnection(deviceId: String) {
        if (!pendingAuth.containsKey(deviceId)) return fail("NO_PENDING_REQUEST", "There is no pending authentication request for this device.")
        client.acceptConnection(deviceId, payloadCallback).addOnFailureListener { fail("ACCEPT_FAILED", it.localizedMessage ?: "Could not accept connection.", it) }
    }
    override fun rejectConnection(deviceId: String) {
        client.rejectConnection(deviceId).addOnFailureListener { fail("REJECT_FAILED", it.localizedMessage ?: "Could not reject connection.", it) }
        pendingAuth.remove(deviceId)
        val d = device(deviceId)
        emit { listener.onAuthenticationStateChanged(d, AuthenticationState.REJECTED); listener.onPeerStateChanged(d, PeerState.REJECTED) }
    }
    override fun sendMessage(deviceId: String, text: String) {
        if (!authenticated.contains(deviceId) || !connected.contains(deviceId)) return fail("NOT_AUTHENTICATED", "Cannot send: peer is not connected and authenticated.")
        if (text.isBlank()) return
        val now = System.currentTimeMillis()
        val json = JSONObject().put("type", "chat").put("text", text).put("time", now).toString()
        val payload = Payload.fromBytes(json.toByteArray(StandardCharsets.UTF_8))
        client.sendPayload(deviceId, payload).addOnSuccessListener {
            emit { listener.onMessage(ChatMessage(deviceId, text, true, now)) }
        }.addOnFailureListener { fail("SEND_FAILED", it.localizedMessage ?: "Message transmission failed.", it) }
    }
    override fun disconnect(deviceId: String) { client.disconnectFromEndpoint(deviceId); connected.remove(deviceId); authenticated.remove(deviceId); pendingAuth.remove(deviceId) }
    override fun disconnectAll() { client.stopAllEndpoints(); connected.clear(); authenticated.clear(); pendingAuth.clear() }
    override fun shutdown() {
        if (closed) return
        stopDiscovery(); stopAdvertising(); disconnectAll(); closed = true
    }
}
