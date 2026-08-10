package com.example.motionlab.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motionlab.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState.asStateFlow()

    fun changePassword(username: String, oldPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading
            if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                _changePasswordState.value = ChangePasswordState.Error("Please fill in all fields.")
                return@launch
            }
            if (newPassword != confirmPassword) {
                _changePasswordState.value = ChangePasswordState.Error("New passwords do not match.")
                return@launch
            }
            val account = authRepository.getAccountByUsername(username)
            if (account == null) {
                _changePasswordState.value = ChangePasswordState.Error("Account not found.")
                return@launch
            }
            if (account.password != oldPassword) {
                _changePasswordState.value = ChangePasswordState.Error("Current password is incorrect.")
                return@launch
            }
            authRepository.updatePassword(username, newPassword)
            _changePasswordState.value = ChangePasswordState.Success
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }
}