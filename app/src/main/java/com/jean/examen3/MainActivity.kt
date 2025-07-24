package com.jean.examen3

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.jean.examen3.presentation.AppViewModel
import com.jean.examen3.presentation.CovidContactTracingApp
import com.jean.examen3.ui.theme.Examen3Theme
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Examen3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CovidContactTracingApp(Modifier.padding(innerPadding), )
                }
            }
        }
    }
}
