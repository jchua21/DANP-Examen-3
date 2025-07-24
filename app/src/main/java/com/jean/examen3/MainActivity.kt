package com.jean.examen3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jean.examen3.services.BleAdvertiseService
import com.jean.examen3.presentation.CovidContactTracingApp
import com.jean.examen3.ui.theme.Examen3Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val REQ_PERMISSIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        // 1) Pedir permisos una vez (antes de inflar la UI)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            REQ_PERMISSIONS
        )

        // 2) Inflar la UI
        setContent {
            Examen3Theme {
                CovidContactTracingApp()
            }
        }
    }

    // 3) Manejar la respuesta de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,       // <— aquí debe ser Array<String>, no Array<out String>
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_PERMISSIONS &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            // Arrancar el service que emite tu UUID por BLE
            ContextCompat.startForegroundService(
                this,
                Intent(this, BleAdvertiseService::class.java)
            )
            // El scanner BLE arrancará desde tu ViewModel/Composable
        } else {
            Toast.makeText(
                this,
                "Es necesario conceder permisos de Bluetooth y ubicación",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestNotificationPermission() {
        // Para Android 13+ solicitar POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQ_PERMISSIONS
            )
        }

        // Verificar si las notificaciones están habilitadas
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (!nm.areNotificationsEnabled()) {
            startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
            )
        }
    }
}
