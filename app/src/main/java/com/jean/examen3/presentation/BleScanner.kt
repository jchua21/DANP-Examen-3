package com.jean.examen3.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import kotlin.math.pow

class BleScanner(private val context: Context) {

    private val scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val rssi = result.rssi
            val deviceAddress = result.device.address
            val distance = estimateDistance(rssi)
            Log.d("BLE_SCAN", "Dispositivo: $deviceAddress, RSSI: $rssi, Distancia: $distance m")

            if (distance <= 30.0) {
                // Aquí puedes registrar el contacto (tiempo, UUID, etc.)
            }
        }
    }

    fun startScan() {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filters = listOf<ScanFilter>() // puedes añadir filtros si usas UUIDs específicos

        scanner.startScan(filters, scanSettings, scanCallback)
    }

    fun stopScan() {
        scanner.stopScan(scanCallback)
    }

    private fun estimateDistance(rssi: Int, txPower: Int = -59): Double {
        return 10.0.pow((txPower - rssi) / (10 * 2.0)) // path-loss exponent 2 (free space)
    }
}
