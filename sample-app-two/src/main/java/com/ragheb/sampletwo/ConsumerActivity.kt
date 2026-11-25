package com.ragheb.sampletwo

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ragheb.connectivity.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Deliberately separate consumer: integrates only through ConnectivityClient's public API. */
class ConsumerActivity : AppCompatActivity(), ConnectivityListener {
    private lateinit var api: ConnectivityClient
    private lateinit var output: TextView
    private lateinit var peers: LinearLayout
    private lateinit var emptyPeers: TextView
    private lateinit var input: EditText
    private lateinit var radioStatus: TextView
    private var peerId: String? = null
    private val peerButtons = mutableMapOf<String, Button>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = ConnectivityClient(this, ConnectivityConfig("com.ragheb.localpoc.service", "Consumer Two · ${Build.MODEL}"), this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 20, 20, 20) }
        root.addView(TextView(this).apply { text = "Independent Consumer App 2"; textSize = 23f })
        root.addView(TextView(this).apply { text = "Uses the same connectivity library through its public API; no transport implementation is copied."; textSize = 13f })
        radioStatus = TextView(this).apply { textSize = 13f; setPadding(8, 8, 8, 8) }
        root.addView(radioStatus)
        root.addView(button("Advertise + discover") { if (checkReadiness()) { api.startAdvertising(); api.startDiscovery(); line("Advertising and discovery requested") } })
        root.addView(button("Stop discovery / advertising") { api.stopDiscovery(); api.stopAdvertising(); line("Discovery and advertising stopped") })
        root.addView(button("Open Wi-Fi settings") { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) })
        root.addView(button("Open Bluetooth settings") { startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) })
        root.addView(TextView(this).apply { text = "Discovered devices"; textSize = 15f })
        peers = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        emptyPeers = TextView(this).apply { text = "No nearby devices yet. Start advertising and discovery on both devices."; textSize = 12f }
        peers.addView(emptyPeers)
        root.addView(peers)
        input = EditText(this).apply { hint = "Message to selected peer"; singleLine = true }
        root.addView(input)
        root.addView(button("Send message") { val id = peerId; val text = input.text.toString(); if (id != null && text.isNotBlank()) { api.sendMessage(id, text); input.text.clear() } else line("Choose an authenticated peer first") })
        root.addView(button("Disconnect") { peerId?.let { api.disconnect(it) } ?: line("No selected peer") })
        root.addView(button("Disconnect all") { api.disconnectAll(); peerId = null })
        output = TextView(this).apply { textSize = 13f; text = "Waiting for test actions…" }
        val scroll = ScrollView(this).apply { addView(output) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        refreshRadioStatus()
        requestPermissionsIfNeeded()
    }

    private fun button(text: String, click: () -> Unit) = Button(this).apply {
        this.text = text; isAllCaps = false; setOnClickListener { click() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    private fun line(s: String) { runOnUiThread { output.append("\n[${timeFormat.format(Date())}] $s") } }

    private fun refreshRadioStatus() {
        val wifi = runCatching { (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled }.getOrDefault(false)
        val bluetooth = runCatching { (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.isEnabled == true }.getOrDefault(false)
        radioStatus.text = "Device: ${Build.MODEL}\nWi-Fi: ${if (wifi) "ON" else "OFF"} · Bluetooth: ${if (bluetooth) "ON" else "OFF"}\nGoogle Play services required; internet is not required for local communication"
    }

    private fun checkReadiness(): Boolean {
        val missing = requiredPermissions().filter { checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            AlertDialog.Builder(this).setTitle("Permissions required")
                .setMessage("Nearby-device permissions are required for discovery and advertising. Grant them, then retry.")
                .setPositiveButton("Grant") { _, _ -> requestPermissions(missing.toTypedArray(), 701) }
                .setNegativeButton("Cancel", null).show()
            return false
        }
        refreshRadioStatus()
        val wifi = runCatching { (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled }.getOrDefault(false)
        val bluetooth = runCatching { (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.isEnabled == true }.getOrDefault(false)
        if (!wifi || !bluetooth) {
            AlertDialog.Builder(this).setTitle("Enable device radios")
                .setMessage("Enable Wi-Fi and Bluetooth manually in Android Settings. This app does not toggle radios automatically.")
                .setPositiveButton("OK", null).show()
            return false
        }
        return true
    }

    private fun requiredPermissions(): List<String> = buildList {
        if (Build.VERSION.SDK_INT >= 31) {
            add(Manifest.permission.BLUETOOTH_SCAN); add(Manifest.permission.BLUETOOTH_CONNECT); add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.NEARBY_WIFI_DEVICES)
        else if (Build.VERSION.SDK_INT >= 29) add(Manifest.permission.ACCESS_FINE_LOCATION)
        else add(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun requestPermissionsIfNeeded() {
        val missing = requiredPermissions().filter { checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 701)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 701) {
            val granted = grantResults.isNotEmpty() && grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }
            line(if (granted) "Nearby permissions granted" else "Permission denied; discovery/advertising is blocked until required permissions are granted")
            refreshRadioStatus()
        }
    }

    override fun onDeviceFound(device: NearbyDevice) {
        runOnUiThread {
            if (peerButtons.containsKey(device.id)) return@runOnUiThread
            if (peerButtons.isEmpty()) peers.removeView(emptyPeers)
            val row = button("Connect to ${device.name}") { peerId = device.id; api.requestConnection(device.id) }
            peerButtons[device.id] = row
            peers.addView(row)
        }
    }
    override fun onDeviceLost(deviceId: String) {
        runOnUiThread {
            peerButtons.remove(deviceId)?.let { peers.removeView(it) }
            if (peerButtons.isEmpty() && emptyPeers.parent == null) peers.addView(emptyPeers)
        }
        line("Device left range: $deviceId")
    }
    override fun onConnectionRequest(request: PeerConnectionRequest) {
        runOnUiThread {
            AlertDialog.Builder(this).setTitle("Verify peer")
                .setMessage("Compare this code with the other device. Accept only if both screens show the same code:\n\n${request.verificationCode}")
                .setPositiveButton("Codes match — accept") { _, _ -> peerId = request.device.id; api.acceptConnection(request.device.id) }
                .setNegativeButton("Reject") { _, _ -> api.rejectConnection(request.device.id) }.show()
        }
    }
    override fun onPeerStateChanged(device: NearbyDevice, state: PeerState) { line("${device.name}: $state"); if (state == PeerState.DISCONNECTED || state == PeerState.REJECTED || state == PeerState.FAILED) if (peerId == device.id) peerId = null }
    override fun onAuthenticationStateChanged(device: NearbyDevice, state: AuthenticationState) { line("Authentication ${device.name}: $state") }
    override fun onDiscoveryStateChanged(state: DiscoveryState) { line("Discovery: $state") }
    override fun onMessage(message: ChatMessage) { line("${if (message.sentByLocalDevice) "ME" else "PEER"}: ${message.text}") }
    override fun onFailure(error: ConnectivityFailure) { line("ERROR ${error.code}: ${error.message}") }
    override fun onResume() { super.onResume(); refreshRadioStatus() }
    override fun onStop() { api.stopDiscovery(); api.stopAdvertising(); super.onStop() }
    override fun onDestroy() { api.shutdown(); super.onDestroy() }
}
