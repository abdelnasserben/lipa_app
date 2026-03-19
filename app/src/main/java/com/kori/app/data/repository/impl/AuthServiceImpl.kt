package com.kori.app.data.repository.impl

import android.app.Activity
import android.content.Intent
import com.kori.app.core.model.auth.AuthState
import com.kori.app.data.datasource.AuthDataSource
import com.kori.app.data.repository.AuthService
import kotlinx.coroutines.flow.StateFlow

class AuthServiceImpl(
    private val dataSource: AuthDataSource,
) : AuthService {
    override val authState: StateFlow<AuthState> = dataSource.authState

    override fun startLogin(activity: Activity) = dataSource.startLogin(activity)

    override suspend fun handleAuthorizationResponse(intent: Intent) =
        dataSource.handleAuthorizationResponse(intent)

    override suspend fun getValidAccessToken(): String? = dataSource.getValidAccessToken()

    override fun isAuthenticated(): Boolean = dataSource.isAuthenticated()

    override fun logout(activity: Activity) = dataSource.logout(activity)

    override fun clearSession() = dataSource.clearSession()
}
