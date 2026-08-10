package com.example.motionlab.presentation.login

import com.example.motionlab.data.local.entity.Account

sealed class LoginState {
    object Idle : LoginState()
    data class Success(val account: Account) : LoginState()
    data class Error(val message: String) : LoginState()
}

