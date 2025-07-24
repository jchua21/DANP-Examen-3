package com.jean.examen3.services

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.jean.examen3.data.local.UserDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
//EMISOR
@Singleton
class BleAdvertiser @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDataStore: UserDataStore
) {
    // Obtiene el objeto que permite hacer advertising BLE (si el dispositivo lo soporta)
    private val advertiser: BluetoothLeAdvertiser? =
        BluetoothAdapter.getDefaultAdapter()?.bluetoothLeAdvertiser

    // Callback para manejar eventos de éxito o error al iniciar el advertising
    private var callback: AdvertiseCallback? = null

    // Configura cómo se transmitirá la señal BLE
    private val settings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER) // Bajo consumo de energía
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM) // Potencia media de señal
        .setConnectable(false) // No permite conexión, solo emite la señal
        .setTimeout(0) // 0 = sin límite de tiempo, se mantiene transmitiendo hasta que se detenga manualmente
        .build()

    private fun getServiceUuid(): UUID {
        return runBlocking {
            val userId = userDataStore.getUserId().firstOrNull()
            if (userId != null) {
                try {
                    UUID.fromString(userId)
                } catch (e: IllegalArgumentException) {
                    Log.w("BleAdvertiser", "Invalid UUID format for user ID: $userId, using default")
                    UUID.fromString("12345678-1234-1234-1234-1234567890ab")
                }
            } else {
                Log.w("BleAdvertiser", "No user ID found, using default UUID")
                UUID.fromString("12345678-1234-1234-1234-1234567890ab")
            }
        }
    }

    // Crea los datos que se van a emitir en la señal BLE (dinámicamente)
    private fun createAdvertiseData(): AdvertiseData {
        return AdvertiseData.Builder()
            .setIncludeDeviceName(false) // No incluye el nombre del dispositivo en la señal
            .addServiceUuid(ParcelUuid(getServiceUuid())) // Emite el UUID del usuario como identificador
            .build()
    }

    // Función para iniciar el advertising BLE
    @SuppressLint("MissingPermission")
    fun startAdvertising() {
        if (advertiser == null) {
            Log.e("BleAdvertiser", "BLE Advertising not supported on this device")
            return
        }

        // Create dynamic data with current user UUID
        val data = createAdvertiseData()
        val userUuid = getServiceUuid()
        Log.d("BleAdvertiser", "Starting advertising with UUID: $userUuid")

        // Define lo que sucede cuando el advertising empieza o falla
        callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.i("BleAdvertiser", "Advertising started successfully with UUID: $userUuid")
            }

            override fun onStartFailure(errorCode: Int) {
                Log.e("BleAdvertiser", "Advertising failed with error code: $errorCode")
            }
        }

        // Inicia la emisión de la señal BLE
        try {
            advertiser.startAdvertising(settings, data, callback)
        } catch (e: SecurityException) {
            Log.e("BleAdvertiser", "Security exception starting advertising: ${e.message}")
        } catch (e: Exception) {
            Log.e("BleAdvertiser", "Error starting advertising: ${e.message}")
        }
    }

    // Función para detener el advertising BLE
    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        try {
            advertiser?.stopAdvertising(callback)
            Log.i("BleAdvertiser", "Advertising stopped")
        } catch (e: SecurityException) {
            Log.e("BleAdvertiser", "Security exception stopping advertising: ${e.message}")
        } catch (e: Exception) {
            Log.e("BleAdvertiser", "Error stopping advertising: ${e.message}")
        }
    }
}