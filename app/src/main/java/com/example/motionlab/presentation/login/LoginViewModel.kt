package com.example.motionlab.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motionlab.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank())
                {
                    _loginState.value = LoginState.Error("Please fill in all fields")
                    return@launch
                }
            val account = repo.signIn(username, password)
            _loginState.value = if (account != null) {
                LoginState.Success(account)
            } else {
                LoginState.Error("Invalid username or password")
            }
        }
    }
    fun resetState() {
        _loginState.value = LoginState.Idle
    }

}
