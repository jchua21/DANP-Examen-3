package com.jean.examen3.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import androidx.annotation.RequiresPermission
import android.Manifest

/**
 * Clase genérica para escanear dispositivos BLE cercanos.
 *
 * @param context Contexto para acceder a BluetoothLeScanner.
 */
class BleScanner(private val context: Context) {



    private var onDetected: ((detectedId: String, rssi: Int, timestamp: String) -> Unit)? = null
    private val scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val rssi = result.rssi
            val detectedId = parseUuid(result) ?: return // Si no hay UUID, no procesar
            val timestamp = getCurrentIsoTimestamp()
            val distance = estimateDistance(rssi)

            // Filtrar por distancia aproximada (ej. <= 30 metros)
            if (distance <= 30.0) {
                Log.d("BleScanner", "Detected $detectedId | RSSI=$rssi | Dist=$distance m | at $timestamp")
                onDetected?.invoke(detectedId, rssi, timestamp)
            }
        }
    }

    /**
     * Inicia el escaneo BLE.
     *
     * @param onDetected Callback que recibe: (id detectado, rssi, timestamp).
     */
    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION  // si quieres incluir ubicación
    ])
    fun startScan(onDetected: (detectedId: String, rssi: Int, timestamp: String) -> Unit) {
        this.onDetected = onDetected
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        val filters = listOf<ScanFilter>()
        scanner.startScan(filters, settings, scanCallback)
    }

    /**
     * Detiene el escaneo BLE.
     */

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        scanner.stopScan(scanCallback)
    }

    /**
     * Extrae el UUID del servicio anunciado o, si no existe, usa la dirección MAC.
     */
    private fun parseUuid(result: ScanResult): String? {
        val serviceUuids = result.scanRecord?.serviceUuids
        return serviceUuids?.firstOrNull()?.uuid?.toString()
    }

    /**
     * Estima la distancia aproximada en metros a partir de RSSI y TX Power.
     */
    private fun estimateDistance(rssi: Int, txPower: Int = -59): Double {
        return 10.0.pow((txPower - rssi) / (10 * 2.0))
    }

    /**
     * Obtiene timestamp en formato ISO 8601 UTC.
     */
    private fun getCurrentIsoTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
            .format(Date())
    }
}
