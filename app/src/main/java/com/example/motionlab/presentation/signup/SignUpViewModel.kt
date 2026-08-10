package com.example.motionlab.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motionlab.data.local.entity.Account
import com.example.motionlab.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    fun signUp(account: Account) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading

            // Input validation
            if (account.username.isBlank() ||
                account.firstname.isBlank() ||
                account.lastname.isBlank() ||
                account.password.isBlank()
            ) {
                _signUpState.value = SignUpState.Error("Please fill in all fields.")
                return@launch
            }

            if (repo.accountExists(account.username)) {
                _signUpState.value = SignUpState.Error("Username already taken")
            } else {
                repo.signUp(account)
                _signUpState.value = SignUpState.Success
            }
        }
    }
}
