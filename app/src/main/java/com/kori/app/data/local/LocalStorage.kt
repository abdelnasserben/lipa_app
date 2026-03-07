package com.kori.app.data.local

import com.kori.app.core.model.UserRole
import com.kori.app.core.model.auth.AuthSession

interface LocalStorage {
    fun getActiveRole(): UserRole?

    fun setActiveRole(role: UserRole?)

    fun getLanguageCode(): String

    fun setLanguageCode(languageCode: String)

    fun isNotificationsEnabled(): Boolean

    fun setNotificationsEnabled(enabled: Boolean)

    fun getAuthSession(): AuthSession?

    fun setAuthSession(session: AuthSession?)
}
