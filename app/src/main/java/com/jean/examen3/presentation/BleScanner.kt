package com.jean.examen3.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow

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
            val detectedId = parseUuid(result)
            val timestamp = getCurrentIsoTimestamp()
            val distance = estimateDistance(rssi)

            Log.d("BleScanner", "Scan result: UUID=$detectedId, RSSI=$rssi, Distance=$distance m")

            if (detectedId == null) {
                Log.w("BleScanner", "No valid UUID found in scan result, skipping")
                return
            }

            // Filtrar por distancia aproximada (ej. <= 30 metros)
            if (distance <= 30.0) {
                Log.d("BleScanner", "Valid contact detected: $detectedId | RSSI=$rssi | Dist=$distance m | at $timestamp")
                onDetected?.invoke(detectedId, rssi, timestamp)
            } else {
                Log.d("BleScanner", "Contact too far: $detectedId | Distance=$distance m, skipping")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BleScanner", "Scan failed with error code: $errorCode")
        }
    }

    /**
     * Inicia el escaneo BLE.
     *
     * @param onDetected Callback que recibe: (id detectado, rssi, timestamp).
     */
    @SuppressLint("MissingPermission")
    fun startScan(onDetected: (detectedId: String, rssi: Int, timestamp: String) -> Unit) {
        this.onDetected = onDetected
        
        if (scanner == null) {
            Log.e("BleScanner", "BluetoothLeScanner is null - Bluetooth not available")
            return
        }
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        val filters = listOf<ScanFilter>()
        
        Log.d("BleScanner", "Starting BLE scan...")
        try {
            scanner.startScan(filters, settings, scanCallback)
            Log.d("BleScanner", "BLE scan started successfully")
        } catch (e: SecurityException) {
            Log.e("BleScanner", "Security exception starting scan: ${e.message}")
        } catch (e: Exception) {
            Log.e("BleScanner", "Error starting scan: ${e.message}")
        }
    }

    /**
     * Detiene el escaneo BLE.
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (scanner == null) {
            Log.e("BleScanner", "BluetoothLeScanner is null - cannot stop scan")
            return
        }
        
        try {
            scanner.stopScan(scanCallback)
            Log.d("BleScanner", "BLE scan stopped successfully")
        } catch (e: SecurityException) {
            Log.e("BleScanner", "Security exception stopping scan: ${e.message}")
        } catch (e: Exception) {
            Log.e("BleScanner", "Error stopping scan: ${e.message}")
        }
    }

    /**
     * Extrae el UUID del servicio anunciado o, si no existe, usa la dirección MAC.
     */
    private fun parseUuid(result: ScanResult): String? {
        val serviceUuids = result.scanRecord?.serviceUuids
        val serviceUuid = serviceUuids?.firstOrNull()?.uuid?.toString()
        
        if (serviceUuid != null) {
            Log.d("BleScanner", "Found service UUID: $serviceUuid")
            return serviceUuid
        }
        
        // Fallback: usar la dirección MAC del dispositivo como identificador
        val deviceAddress = result.device?.address
        if (deviceAddress != null) {
            Log.d("BleScanner", "No service UUID found, using device address: $deviceAddress")
            return deviceAddress
        }
        
        Log.w("BleScanner", "No UUID or device address found")
        return null
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
