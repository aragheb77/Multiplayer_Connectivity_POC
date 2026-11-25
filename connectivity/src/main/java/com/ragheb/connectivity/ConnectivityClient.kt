package com.ragheb.connectivity

import android.content.Context
import com.ragheb.connectivity.internal.TransportFactory

/**
 * Public facade consumed by host apps. No Google Nearby types escape this API.
 * Call methods from the main thread. Listener callbacks are delivered on the main thread.
 */
class ConnectivityClient(
    context: Context,
    config: ConnectivityConfig,
    private val listener: ConnectivityListener
) {
    private val transport = TransportFactory.create(context.applicationContext, config, listener)

    fun startAdvertising() = transport.startAdvertising()
    fun stopAdvertising() = transport.stopAdvertising()
    fun startDiscovery() = transport.startDiscovery()
    fun stopDiscovery() = transport.stopDiscovery()
    fun requestConnection(deviceId: String) = transport.requestConnection(deviceId)
    /** Accept only after users compare the same verification code on both devices. */
    fun acceptConnection(deviceId: String) = transport.acceptConnection(deviceId)
    fun rejectConnection(deviceId: String) = transport.rejectConnection(deviceId)
    fun sendMessage(deviceId: String, text: String) = transport.sendMessage(deviceId, text)
    fun disconnect(deviceId: String) = transport.disconnect(deviceId)
    fun disconnectAll() = transport.disconnectAll()
    fun shutdown() = transport.shutdown()
}
