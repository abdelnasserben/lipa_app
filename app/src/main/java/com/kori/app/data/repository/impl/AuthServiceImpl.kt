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

    override fun beginAuthentication(activity: Activity) = dataSource.beginAuthentication(activity)

    override suspend fun completeAuthenticationFromIntent(intent: Intent) =
        dataSource.completeAuthenticationFromIntent(intent)

    override suspend fun refreshSessionIfNeeded() = dataSource.refreshSessionIfNeeded()

    override fun logout() = dataSource.logout()
}
