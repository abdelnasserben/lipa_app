package com.kori.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.app.Activity
import android.content.Intent
import com.kori.app.core.model.auth.AuthState
import com.kori.app.data.repository.AuthService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authService: AuthService,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authService.authState

    fun beginLogin(activity: Activity) {
        authService.beginAuthentication(activity)
    }

    fun completeLogin(intent: Intent) {
        viewModelScope.launch {
            authService.completeAuthenticationFromIntent(intent)
        }
    }

    fun refreshSessionIfNeeded() {
        viewModelScope.launch {
            authService.refreshSessionIfNeeded()
        }
    }

    fun logout() {
        authService.logout()
    }

    companion object {
        fun factory(
            authService: AuthService,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(
                        authService = authService,
                    ) as T
                }
            }
        }
    }
}
