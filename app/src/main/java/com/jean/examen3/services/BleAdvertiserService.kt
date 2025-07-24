package com.jean.examen3.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BleAdvertiseService : Service() {

    // Inyección de la clase responsable de emitir BLE
    @Inject lateinit var bleAdvertiser: BleAdvertiser

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate!!!")
        // Inicia el servicio en modo "foreground" (prioridad alta) con una notificación visible
        startForeground(NOTIF_ID, createNotification())

        // Inicia la emisión BLE
        bleAdvertiser.startAdvertising()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detiene la emisión BLE cuando se destruye el servicio
        bleAdvertiser.stopAdvertising()
    }

    // Este servicio no está diseñado para "bindearse", por eso retorna null
    override fun onBind(intent: Intent?): IBinder? = null

    // Crea una notificación persistente para que el sistema no mate el servicio
    private fun createNotification(): Notification {
        Log.d(TAG, "createNotification: Creando Notificacion!!!")
        val channelId = "ble_advertise_channel"

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // NUEVO: Verificar si las notificaciones están habilitadas
        Log.d(TAG, "Notificaciones habilitadas para la app: ${manager.areNotificationsEnabled()}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotification: Entrando IF (Android 8+)!!!")

            // Verificar si el canal ya existe
            val existingChannel = manager.getNotificationChannel(channelId)
            Log.d(TAG, "Canal existente: $existingChannel")

            val channel = NotificationChannel(
                channelId,
                "Transmisión BLE",
                NotificationManager.IMPORTANCE_DEFAULT // CAMBIADO DE LOW A DEFAULT
            ).apply {
                description = "Canal para el servicio de transmisión BLE"
                setShowBadge(true)
            }

            manager.createNotificationChannel(channel)

            // Verificar que el canal se creó
            val createdChannel = manager.getNotificationChannel(channelId)
            Log.d(TAG, "Canal después de crear: $createdChannel")
            Log.d(TAG, "Importancia del canal: ${createdChannel?.importance}")
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Transmisión BLE activa")
            .setContentText("Tu dispositivo está emitiendo señal BLE.")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // CAMBIADO DE LOW A DEFAULT
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        Log.d(TAG, "Notificación creada: $notification")

        return notification

    }

    companion object {
        const val TAG = "BLE_ADVERTISER_SERVICE"
        const val NOTIF_ID = 101 // ID único para la notificación del servicio
    }
}
