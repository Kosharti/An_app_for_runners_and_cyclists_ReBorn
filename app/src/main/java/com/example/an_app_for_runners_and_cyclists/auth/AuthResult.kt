package com.example.an_app_for_runners_and_cyclists.auth

import com.example.an_app_for_runners_and_cyclists.data.model.User

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}