package com.jean.examen3.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CovidContactTracingApp(
    modifier: Modifier = Modifier
) {
    val vm   = hiltViewModel<AppViewModel>()
    val ui   by vm.uiState.collectAsStateWithLifecycle()

    // En cuanto sepas que el usuario ya está registrado:
    LaunchedEffect(ui.isAlreadyRegistered) {
        if (ui.isAlreadyRegistered) vm.startDetection()
        else                       vm.stopDetection()
    }

    val viewModel = hiltViewModel<AppViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        // Show loading while checking registration status
        uiState.isCheckingRegistration -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // User is already registered, show main screen
        uiState.isAlreadyRegistered -> {
            MainScreen()
        }
        
        // User is not registered, show registration screen
        else -> {
            RegisterScreen(
                viewModel = viewModel,
                onSuccessRegister = { _, _ ->
                    // Registration success is handled by the ViewModel
                    // The state will automatically update to show MainScreen
                }
            )
        }
    }
}