package com.jean.examen3.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jean.examen3.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        checkRegistrationStatus()
    }

    private fun checkRegistrationStatus() {
        viewModelScope.launch {
            userRepository.isUserRegistered().collect { isRegistered ->
                _uiState.value = _uiState.value.copy(
                    isCheckingRegistration = false,
                    isAlreadyRegistered = isRegistered
                )
            }
        }
        
        // Load user data if registered
        viewModelScope.launch {
            userRepository.getUserIdFromLocal().collect { userId ->
                if (userId != null) {
                    _uiState.value = _uiState.value.copy(registeredUserId = userId)
                }
            }
        }
    }

    fun updateFirstName(firstName: String) {
        // Solo permitir letras y espacios
        if (firstName.all { it.isLetter() || it.isWhitespace() }) {
            val isValid = firstName.trim().length >= 2
            _uiState.value = _uiState.value.copy(
                firstName = firstName,
                isValidFirstName = isValid
            )
        }
    }

    fun updateLastName(lastName: String) {
        // Solo permitir letras y espacios
        if (lastName.all { it.isLetter() || it.isWhitespace() }) {
            val isValid = lastName.trim().length >= 2
            _uiState.value = _uiState.value.copy(
                lastName = lastName,
                isValidLastName = isValid
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

                val result = userRepository.registerUser(
                    firstName = currentState.firstName.trim(),
                    lastName = currentState.lastName.trim()
                )
                
                result.fold(
                    onSuccess = { user ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            registeredUserId = user.id,
                            isAlreadyRegistered = true // This will trigger navigation to MainScreen
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al registrar usuario: ${exception.message}"
                        )
                    }
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}
