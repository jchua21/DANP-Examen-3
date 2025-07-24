package com.jean.examen3.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jean.examen3.data.local.UserDataStore
import com.jean.examen3.data.repository.DetectedContactRepository
import com.jean.examen3.data.repository.UserRepository
import com.jean.examen3.services.BleAdvertiser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import javax.inject.Inject
import android.content.Context


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
    val isAlreadyRegistered: Boolean = false,
    val isScanningActive: Boolean = false
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
    private val bleAdvertiser: BleAdvertiser,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Job para controlar el escaneo periódico
    private var periodicScanJob: Job? = null
    private var isScanning = false

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

    /** Inicia el escaneo BLE periódico cada minuto */
    fun startDetection() {
        // Cancela cualquier escaneo previo
        periodicScanJob?.cancel()
        
        periodicScanJob = viewModelScope.launch {
            try {
                // Check permissions first
                if (!hasBluetoothPermissions()) {
                    android.util.Log.e("AppViewModel", "Missing Bluetooth permissions for scanning")
                    return@launch
                }

                val myId = userDataStore.getUserId().firstOrNull()
                if (myId == null) {
                    android.util.Log.e("AppViewModel", "No user ID found, cannot start detection")
                    return@launch
                }
                
                android.util.Log.d("AppViewModel", "Starting periodic BLE detection (every 1 minute) with user ID: $myId")
                
                // Lista temporal para almacenar contactos detectados en este ciclo
                val detectedContacts = mutableSetOf<String>()
                
                while (true) {
                    try {
                        isScanning = true
                        android.util.Log.d("AppViewModel", "Starting 1-minute scan cycle...")
                        _uiState.value = _uiState.value.copy(isScanningActive = true)
                        
                        // Limpiar contactos del ciclo anterior
                        detectedContacts.clear()
                        
                        bleScanner.startScan { detectedId, rssi, timestamp ->
                            // Solo procesar si no hemos detectado este contacto en este ciclo
                            if (detectedContacts.add(detectedId)) {
                                // Launch a new coroutine for each unique contact detection
                                viewModelScope.launch {
                                    try {
                                        android.util.Log.d("AppViewModel", "New contact detected in cycle: $detectedId with RSSI: $rssi")
                                        val result = detectedContactRepo.insertDetectedContact(
                                            detectorUserId = myId,
                                            detectedUserId = detectedId,
                                            rssi = rssi,
                                            detectedAt = timestamp
                                        )
                                        result.fold(
                                            onSuccess = { 
                                                android.util.Log.d("AppViewModel", "Contact saved successfully: ${it.id}")
                                            },
                                            onFailure = { 
                                                android.util.Log.e("AppViewModel", "Failed to save contact: ${it.message}")
                                            }
                                        )
                                    } catch (e: Exception) {
                                        android.util.Log.e("AppViewModel", "Error saving contact: ${e.message}")
                                    }
                                }
                            }
                        }
                        
                        // Escanear por 1 minuto (60 segundos)
                        delay(60_000)
                        
                        // Detener el escaneo
                        bleScanner.stopScan()
                        isScanning = false
                        _uiState.value = _uiState.value.copy(isScanningActive = false)
                        
                        android.util.Log.d("AppViewModel", "Completed scan cycle. Detected ${detectedContacts.size} unique contacts")
                        
                        // Pequeña pausa antes del siguiente ciclo (opcional, para evitar sobrecarga)
                        delay(1_000) // 1 segundo de pausa
                        
                    } catch (e: Exception) {
                        android.util.Log.e("AppViewModel", "Error during scan cycle: ${e.message}")
                        isScanning = false
                        // Esperar antes de reintentar
                        delay(5_000) // 5 segundos antes de reintentar
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "Error starting periodic detection: ${e.message}")
                isScanning = false
            }
        }
    }

    /** Detiene el escaneo BLE periódico */
    fun stopDetection() {
        try {
            android.util.Log.d("AppViewModel", "Stopping periodic BLE detection")
            
            // Cancelar el job periódico
            periodicScanJob?.cancel()
            periodicScanJob = null
            
            // Detener escaneo actual si está activo
            if (isScanning) {
                if (!hasBluetoothPermissions()) {
                    android.util.Log.e("AppViewModel", "Missing Bluetooth permissions for stopping scan")
                    return
                }
                bleScanner.stopScan()
                isScanning = false
            }
            
            // Actualizar UI state
            _uiState.value = _uiState.value.copy(isScanningActive = false)
            
            android.util.Log.d("AppViewModel", "Periodic BLE detection stopped")
        } catch (e: Exception) {
            android.util.Log.e("AppViewModel", "Error stopping periodic detection: ${e.message}")
        }
    }

    /** Inicia la emisión BLE */
    fun startAdvertising() {
        if (!hasBluetoothPermissions()) {
            android.util.Log.e("AppViewModel", "Missing Bluetooth permissions for advertising")
            return
        }
        bleAdvertiser.startAdvertising()
    }

    /** Detiene la emisión BLE */
    fun stopAdvertising() {
        if (!hasBluetoothPermissions()) {
            android.util.Log.e("AppViewModel", "Missing Bluetooth permissions for stopping advertising")
            return
        }
        bleAdvertiser.stopAdvertising()
    }

    /** Check if app has required Bluetooth permissions */
    private fun hasBluetoothPermissions(): Boolean {
        val bluetoothScan = ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

        val bluetoothAdvertise = ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_ADVERTISE
        ) == PackageManager.PERMISSION_GRANTED

        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return bluetoothScan && bluetoothAdvertise && fineLocation
    }
}
