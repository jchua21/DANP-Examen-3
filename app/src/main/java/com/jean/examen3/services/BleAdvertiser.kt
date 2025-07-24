package com.jean.examen3.services

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import com.jean.examen3.data.local.UserDataStore
import java.util.UUID
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
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

    // UUID que identifica el servicio BLE. Este debe coincidir con el que escanearán los receptores.
    private val SERVICE_UUID = UUID.fromString("12345678-1234-1234-1234-1234567890ab")

    // Configura cómo se transmitirá la señal BLE
    private val settings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER) // Bajo consumo de energía
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM) // Potencia media de señal
        .setConnectable(false) // No permite conexión, solo emite la señal
        .setTimeout(0) // 0 = sin límite de tiempo, se mantiene transmitiendo hasta que se detenga manualmente
        .build()

    // Datos que se van a emitir en la señal BLE
    private val data = AdvertiseData.Builder()
        .setIncludeDeviceName(false) // No incluye el nombre del dispositivo en la señal
        .addServiceUuid(ParcelUuid(getServiceUuid())) // Solo emite el UUID como identificador
        .build()

    private fun getServiceUuid(): UUID {
        return runBlocking {
            val userId = userDataStore.getUserId().firstOrNull()
            if (userId != null) {
                UUID.fromString(userId)
            } else {
                UUID.fromString("12345678-1234-1234-1234-1234567890ab")
            }
        }
    }

    // Función para iniciar el advertising BLE
    fun startAdvertising() {
        if (advertiser == null) {
            Log.e("BleAdvertiser", "BLE Advertising not supported on this device")
            return
        }

        // Define lo que sucede cuando el advertising empieza o falla
        callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.i("BleAdvertiser", "Advertising started") // Se inició correctamente
            }

            override fun onStartFailure(errorCode: Int) {
                Log.e("BleAdvertiser", "Advertising failed: $errorCode") // Falló el inicio
            }
        }

        // Inicia la emisión de la señal BLE
        advertiser.startAdvertising(
            settings,
            data,
            callback
        )
    }

    // Función para detener el advertising BLE
    fun stopAdvertising() {
        advertiser?.stopAdvertising(callback)
        Log.i("BleAdvertiser", "Advertising stopped") // Se detuvo la emisión
    }
}