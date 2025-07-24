package com.jean.examen3.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val nombres: String = "",
    val apellidos: String = "",
    val isValidNombres: Boolean = false,
    val isValidApellidos: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isValidData: Boolean
        get() = isValidNombres && isValidApellidos
}

@HiltViewModel
class AppViewModel @Inject constructor(

) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateNombres(nombres: String) {
        // Solo permitir letras y espacios
        if (nombres.all { it.isLetter() || it.isWhitespace() }) {
            val isValid = nombres.trim().length >= 2
            _uiState.value = _uiState.value.copy(
                nombres = nombres,
                isValidNombres = isValid
            )
        }
    }

    fun updateApellidos(apellidos: String) {
        // Solo permitir letras y espacios
        if (apellidos.all { it.isLetter() || it.isWhitespace() }) {
            val isValid = apellidos.trim().length >= 2
            _uiState.value = _uiState.value.copy(
                apellidos = apellidos,
                isValidApellidos = isValid
            )
        }
    }

    fun registerUser() {
        val currentState = _uiState.value
        if (!currentState.isValidData) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                // Simular envío de datos
                kotlinx.coroutines.delay(2000)

                // Aquí iría la lógica para enviar los datos
                sendDataToServer(currentState.nombres.trim(), currentState.apellidos.trim())

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al registrar: ${e.message}"
                )
            }
        }
    }

    private suspend fun sendDataToServer(nombres: String, apellidos: String) {
        // Aquí va la lógica para enviar los datos al servidor/nube
        println("Enviando datos: $nombres $apellidos")

    }
}
