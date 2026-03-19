package com.kori.app.feature.auth

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.auth.AuthState
import com.kori.app.data.repository.AuthService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authService: AuthService,
) : ViewModel() {

    val authState: StateFlow<AuthState> = authService.authState

    fun startLogin(activity: Activity) {
        authService.startLogin(activity)
    }

    fun handleAuthorizationResponse(intent: Intent) {
        viewModelScope.launch {
            authService.handleAuthorizationResponse(intent)
        }
    }

    fun logout(activity: Activity) {
        authService.logout(activity)
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
