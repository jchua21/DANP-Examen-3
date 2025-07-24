package com.jean.examen3.presentation

import androidx.compose.runtime.Composable
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jean.examen3.services.BleAdvertiseService

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isAdvertising by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isAdvertising) "Emisión BLE activa" else "Emisión BLE inactiva",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val intent = Intent(context, BleAdvertiseService::class.java)
                ContextCompat.startForegroundService(context, intent)
                isAdvertising = true
            },
            enabled = !isAdvertising
        ) {
            Text("Iniciar Emisor BLE")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, BleAdvertiseService::class.java)
                context.stopService(intent)
                isAdvertising = false
            },
            enabled = isAdvertising
        ) {
            Text("Detener Emisor BLE")
        }
    }
}