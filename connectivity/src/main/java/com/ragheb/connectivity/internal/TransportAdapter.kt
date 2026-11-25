package com.ragheb.connectivity.internal

import com.ragheb.connectivity.ConnectivityConfig
import com.ragheb.connectivity.ConnectivityListener
import android.content.Context

/** Internal technology boundary. A future transport implements this without changing host APIs. */
internal interface TransportAdapter {
    fun startAdvertising()
    fun stopAdvertising()
    fun startDiscovery()
    fun stopDiscovery()
    fun requestConnection(deviceId: String)
    fun acceptConnection(deviceId: String)
    fun rejectConnection(deviceId: String)
    fun sendMessage(deviceId: String, text: String)
    fun disconnect(deviceId: String)
    fun disconnectAll()
    fun shutdown()
}

internal object TransportFactory {
    fun create(context: Context, config: ConnectivityConfig, listener: ConnectivityListener): TransportAdapter =
        NearbyConnectionsTransport(context, config, listener)
}
