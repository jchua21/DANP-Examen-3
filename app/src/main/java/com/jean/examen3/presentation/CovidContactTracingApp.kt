package com.jean.examen3.presentation
import android.Manifest
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CovidContactTracingApp(
    modifier: Modifier= Modifier
) {
//    val viewModel = hiltViewModel<AppViewModel>()
//
//    RegisterScreen(
//        viewModel =viewModel
//    )
    MainScreen()
}