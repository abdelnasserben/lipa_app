package com.kori.app.data.repository.impl

import com.kori.app.core.model.auth.AuthState
import com.kori.app.data.datasource.AuthDataSource
import com.kori.app.data.repository.AuthService
import kotlinx.coroutines.flow.StateFlow

class AuthServiceImpl(
    private val dataSource: AuthDataSource,
) : AuthService {
    override val authState: StateFlow<AuthState> = dataSource.authState

    override fun beginAuthentication() = dataSource.beginAuthentication()

    override suspend fun completeAuthenticationSuccess() = dataSource.completeAuthenticationSuccess()

    override fun failAuthentication(message: String) = dataSource.failAuthentication(message)

    override fun logout() = dataSource.logout()
}
