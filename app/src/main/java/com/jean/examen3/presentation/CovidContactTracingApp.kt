package com.jean.examen3.presentation
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CovidContactTracingApp(
    modifier: Modifier= Modifier
) {
    val viewModel = hiltViewModel<AppViewModel>()

//    val context = LocalContext.current
//    val userRepository = remember { UserPreferencesRepository(context) }
//
//    // Observar si el usuario está registrado
//    val isRegistered by userRepository.isUserRegistered.collectAsState(initial = false)
//
//    if (isRegistered) {
//        StatusScreen(userRepository = userRepository)
//    } else {
//        RegisterScreen(userRepository = userRepository)
//    }
    RegisterScreen(
        viewModel =viewModel
    )
}