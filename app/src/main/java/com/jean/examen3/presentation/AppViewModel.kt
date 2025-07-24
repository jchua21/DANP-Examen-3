package com.jean.examen3.presentation

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jean.examen3.data.local.UserDataStore
import com.jean.examen3.data.repository.DetectedContactRepository
import com.jean.examen3.data.repository.UserRepository
import com.jean.examen3.services.BleAdvertiser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.annotation.RequiresPermission
import android.Manifest


/**
 * Estado de UI para registro y detección BLE.
 */
data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val isValidFirstName: Boolean = false,
    val isValidLastName: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val registeredUserId: String? = null,
    val isCheckingRegistration: Boolean = true,
    val isAlreadyRegistered: Boolean = false
) {
    val isValidData: Boolean
        get() = isValidFirstName && isValidLastName
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val detectedContactRepo: DetectedContactRepository,
    private val userDataStore: UserDataStore,
    private val bleScanner: BleScanner,
    private val bleAdvertiser: BleAdvertiser
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        checkRegistrationStatus()
    }


    private fun checkRegistrationStatus() {
        viewModelScope.launch {
            // Comprueba si ya está registrado
            userRepository.isUserRegistered().collect { isReg ->
                _uiState.value = _uiState.value.copy(
                    isCheckingRegistration = false,
                    isAlreadyRegistered = isReg
                )
            }
        }
        // Carga el userId local si existe
        viewModelScope.launch {
            val id = userDataStore.getUserId().firstOrNull()
            if (id != null) {
                _uiState.value = _uiState.value.copy(registeredUserId = id)
            }
        }
    }

    /** Actualiza y valida nombre */
    fun updateFirstName(firstName: String) {
        if (firstName.all { it.isLetter() || it.isWhitespace() }) {
            val valid = firstName.trim().length >= 2
            _uiState.value = _uiState.value.copy(
                firstName = firstName,
                isValidFirstName = valid
            )
        }
    }

    /** Actualiza y valida apellido */
    fun updateLastName(lastName: String) {
        if (lastName.all { it.isLetter() || it.isWhitespace() }) {
            val valid = lastName.trim().length >= 2
            _uiState.value = _uiState.value.copy(
                lastName = lastName,
                isValidLastName = valid
            )
        }
    }

    /** Registra al usuario en la nube y localmente */
    fun registerUser() {
        val state = _uiState.value
        if (!state.isValidData) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            try {
                val result = userRepository.registerUser(
                    firstName = state.firstName.trim(),
                    lastName  = state.lastName.trim()
                )
                result.fold(
                    onSuccess = { user ->
                        _uiState.value = state.copy(
                            isLoading = false,
                            isSuccess = true,
                            registeredUserId = user.id,
                            isAlreadyRegistered = true
                        )
                    },
                    onFailure = { ex ->
                        _uiState.value = state.copy(
                            isLoading = false,
                            errorMessage = "Error al registrar: ${ex.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }


    /** Inicia el escaneo BLE y guarda cada contacto */
    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION
    ])
    fun startDetection() {
        viewModelScope.launch {
            val myId = userDataStore.getUserId().firstOrNull() ?: return@launch
            bleScanner.startScan { detectedId, rssi, timestamp ->
                Log.d("AppViewModel", "Escaneando dispositivo: $detectedId")
                viewModelScope.launch {
                    val result = detectedContactRepo.insertDetectedContact(
                        detectorUserId = myId,
                        detectedUserId = detectedId,
                        rssi = rssi,
                        detectedAt = timestamp
                    )
                    Log.d("AppViewModel", "Resultado BD: ${if(result.isSuccess) "ÉXITO" else "ERROR: ${result.exceptionOrNull()?.message}"}")
                }
            }
        }
    }

    /** Detiene el escaneo BLE */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDetection() {
        bleScanner.stopScan()
    }

    /** Inicia la emisión BLE */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun startAdvertising() {
        Log.d("AppViewModel", "Iniciando advertising BLE")
        bleAdvertiser.startAdvertising()
    }

    /** Detiene la emisión BLE */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun stopAdvertising() {
        Log.d("AppViewModel", "Deteniendo advertising BLE")
        bleAdvertiser.stopAdvertising()
    }

}
