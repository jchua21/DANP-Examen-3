package com.jean.examen3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.jean.examen3.presentation.AppViewModel
import com.jean.examen3.presentation.CovidContactTracingApp
import com.jean.examen3.ui.theme.Examen3Theme
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import android.provider.Settings

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        setContent {
            Examen3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CovidContactTracingApp(Modifier.padding(innerPadding), )
                }
            }

            // Permisos existentes + el nuevo permiso de notificaciones
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.POST_NOTIFICATIONS // AGREGAR ESTE
            )

            ActivityCompat.requestPermissions(this, permissions, 1001)
        }
    }

    private fun requestNotificationPermission() {
        // Para Android 13+ solicitar permiso POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    123)
            }
        }

        // Verificar si las notificaciones están habilitadas en configuración del sistema
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (!notificationManager.areNotificationsEnabled()) {
            // Dirigir al usuario a la configuración de notificaciones
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
        }
    }
}
