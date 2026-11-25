package com.ragheb.sample

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ragheb.connectivity.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), ConnectivityListener {
    private lateinit var client: ConnectivityClient
    private lateinit var status: TextView
    private lateinit var log: TextView
    private lateinit var devicesBox: LinearLayout
    private lateinit var messageInput: EditText
    private var selectedPeer: String? = null
    private val deviceRows = mutableMapOf<String, Button>()
    private var emptyDevicesLabel: TextView? = null
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = ConnectivityClient(this, ConnectivityConfig("com.ragheb.localpoc.service", Build.MODEL ?: "Android device"), this)
        buildScreen()
        refreshRadioStatus()
        requestRequiredPermissions()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 22, 22, 18); setBackgroundColor(0xFFF8FAFC.toInt()) }
        val title = TextView(this).apply { text = "Local Multiplayer POC"; textSize = 24f; setTextColor(0xFF0F172A.toInt()); setTypeface(null, 1) }
        root.addView(title)
        root.addView(TextView(this).apply { text = "Reusable connectivity library · Sample application 1"; textSize = 13f; setTextColor(0xFF64748B.toInt()) })
        status = TextView(this).apply { textSize = 13f; setPadding(12, 12, 12, 12); setTextColor(0xFF1E293B.toInt()); setBackgroundColor(0xFFE2E8F0.toInt()) }
        root.addView(status, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 14; bottomMargin = 10 })
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(actionButton("Start advertising") { if (checkReadiness()) { client.startAdvertising(); note("Advertising requested") } })
        actions.addView(actionButton("Stop advertising") { client.stopAdvertising(); note("Advertising stopped") })
        root.addView(actions)
        val discovery = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        discovery.addView(actionButton("Discover") { if (checkReadiness()) client.startDiscovery() })
        discovery.addView(actionButton("Stop discovery") { client.stopDiscovery() })
        root.addView(discovery)
        root.addView(TextView(this).apply { text = "NEARBY DEVICES (tap to connect)"; textSize = 12f; setTypeface(null, 1); setTextColor(0xFF475569.toInt()); setPadding(0, 16, 0, 6) })
        devicesBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        emptyDevicesLabel = TextView(this).apply { text = "No devices found yet. Start discovery on both devices."; textSize = 12f; setTextColor(0xFF64748B.toInt()) }
        devicesBox.addView(emptyDevicesLabel)
        root.addView(devicesBox)
        root.addView(TextView(this).apply { text = "MESSAGES"; textSize = 12f; setTypeface(null, 1); setTextColor(0xFF475569.toInt()); setPadding(0, 14, 0, 6) })
        val composer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        messageInput = EditText(this).apply { hint = "Type a test message"; textSize = 14f; singleLine = true }
        composer.addView(messageInput, LinearLayout.LayoutParams(0, -2, 1f))
        composer.addView(actionButton("Send") { val peer = selectedPeer; val text = messageInput.text.toString(); if (peer != null && text.isNotBlank()) { client.sendMessage(peer, text); messageInput.text.clear() } else note("Select an authenticated peer first") })
        root.addView(composer)
        val scroll = ScrollView(this)
        log = TextView(this).apply { textSize = 13f; setTextColor(0xFF334155.toInt()); setPadding(10, 8, 10, 8); text = "Ready. Install this app on two Android devices." }
        scroll.addView(log)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        bottom.addView(actionButton("Wi-Fi settings") { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) })
        bottom.addView(actionButton("Bluetooth settings") { startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) })
        bottom.addView(actionButton("Disconnect all") { client.disconnectAll(); selectedPeer = null })
        root.addView(bottom)
        setContentView(root)
    }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply { text = label; textSize = 11f; isAllCaps = false; setPadding(5, 2, 5, 2); setOnClickListener { action() }; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(2, 2, 2, 2) } }
    private fun note(text: String) { runOnUiThread { log.append("\n[${dateFormat.format(Date())}] $text") } }
    private fun refreshRadioStatus() {
        val wifi = runCatching { (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled }.getOrDefault(false)
        val bt = runCatching { (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.isEnabled == true }.getOrDefault(false)
        status.text = "Device: ${Build.MODEL}\nWi-Fi: ${if (wifi) "ON" else "OFF"}  ·  Bluetooth: ${if (bt) "ON" else "OFF"}\nGoogle Play Services required · Internet not required for peer communication"
    }
    private fun checkReadiness(): Boolean {
        val required = requiredPermissions()
        val missing = required.filter { checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) { AlertDialog.Builder(this).setTitle("Permissions required").setMessage("Nearby-device permissions are required for advertising and discovery. Grant them, then retry.").setPositiveButton("OK", null).show(); requestPermissions(missing.toTypedArray(), 700); return false }
        refreshRadioStatus()
        val wifi = runCatching { (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled }.getOrDefault(false)
        val bt = runCatching { (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.isEnabled == true }.getOrDefault(false)
        if (!wifi || !bt) { AlertDialog.Builder(this).setTitle("Enable device radios").setMessage("Nearby Connections may need Wi-Fi and Bluetooth. This app will not switch radios on automatically. Enable the disabled radio(s) in Android Settings, then retry.").setPositiveButton("OK", null).show(); return false }
        return true
    }
    private fun requiredPermissions(): List<String> {
        val required = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 31) { required += Manifest.permission.BLUETOOTH_SCAN; required += Manifest.permission.BLUETOOTH_CONNECT; required += Manifest.permission.BLUETOOTH_ADVERTISE }
        if (Build.VERSION.SDK_INT >= 33) required += Manifest.permission.NEARBY_WIFI_DEVICES
        else if (Build.VERSION.SDK_INT >= 29) required += Manifest.permission.ACCESS_FINE_LOCATION
        else required += Manifest.permission.ACCESS_COARSE_LOCATION
        return required
    }
    private fun requestRequiredPermissions() {
        val required = requiredPermissions()
        val missing = required.filter { checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 700)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) { super.onRequestPermissionsResult(requestCode, permissions, grantResults); if (requestCode == 700) { note(if (grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }) "Permissions granted" else "Required permission denied; discovery/advertising may not work"); refreshRadioStatus() } }

    override fun onDeviceFound(device: NearbyDevice) { runOnUiThread { if (deviceRows.containsKey(device.id)) return@runOnUiThread; emptyDevicesLabel?.let { devicesBox.removeView(it) }; val b = Button(this).apply { text = "${device.name}  ·  Connect"; isAllCaps = false; setOnClickListener { selectedPeer = device.id; client.requestConnection(device.id); note("Connection requested: ${device.name}") } }; deviceRows[device.id] = b; devicesBox.addView(b) } }
    override fun onDeviceLost(deviceId: String) { runOnUiThread { deviceRows.remove(deviceId)?.let { devicesBox.removeView(it) }; if (deviceRows.isEmpty() && emptyDevicesLabel?.parent == null) { devicesBox.addView(emptyDevicesLabel) } }; note("Device left range: $deviceId") }
    override fun onConnectionRequest(request: PeerConnectionRequest) { runOnUiThread { AlertDialog.Builder(this).setTitle("Verify ${request.device.name}").setMessage("Compare this code with the other device.\n\n${request.verificationCode}\n\nAccept only if both screens show exactly the same code.").setPositiveButton("Codes match — accept") { _, _ -> selectedPeer = request.device.id; client.acceptConnection(request.device.id) }.setNegativeButton("Reject") { _, _ -> client.rejectConnection(request.device.id) }.setCancelable(false).show() } }
    override fun onPeerStateChanged(device: NearbyDevice, state: PeerState) { note("${device.name}: $state"); if (state == PeerState.DISCONNECTED || state == PeerState.REJECTED) { if (selectedPeer == device.id) selectedPeer = null } }
    override fun onAuthenticationStateChanged(device: NearbyDevice, state: AuthenticationState) { note("Authentication ${device.name}: $state") }
    override fun onDiscoveryStateChanged(state: DiscoveryState) { note("Discovery: $state") }
    override fun onMessage(message: ChatMessage) { note("${if (message.sentByLocalDevice) "ME" else "PEER"}: ${message.text}") }
    override fun onFailure(error: ConnectivityFailure) { note("ERROR ${error.code}: ${error.message}") }
    override fun onResume() { super.onResume(); refreshRadioStatus() }
    override fun onStop() { client.stopDiscovery(); client.stopAdvertising(); super.onStop() }
    override fun onDestroy() { client.shutdown(); super.onDestroy() }
}
