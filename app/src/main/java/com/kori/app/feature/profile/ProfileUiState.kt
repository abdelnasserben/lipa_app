package com.kori.app.feature.profile

import com.kori.app.core.model.UserRole
import com.kori.app.core.model.auth.AuthSession

enum class ConnectionState(
    val label: String,
) {
    CONNECTED("Connecté"),
    CONNECTING("Connexion en cours"),
    DISCONNECTED("Déconnecté"),
    ERROR("Erreur de session"),
}

enum class AppLanguage(
    val label: String,
) {
    FR("Français"),
    EN("English");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.name == code } ?: FR
        }
    }
}

enum class AppThemeMode(
    val label: String,
) {
    LIGHT_ONLY("Clair uniquement"),
}

data class ProfileCardUiModel(
    val displayName: String,
    val code: String,
    val status: String,
    val createdAt: String,
    val phone: String? = null,
    val sessionSubject: String? = null,
)

data class SessionSummaryUiModel(
    val connectionState: ConnectionState,
    val issuer: String? = null,
    val subject: String? = null,
    val expiration: String? = null,
) {
    val isConnected: Boolean = connectionState == ConnectionState.CONNECTED

    companion object {
        fun disconnected(): SessionSummaryUiModel {
            return SessionSummaryUiModel(connectionState = ConnectionState.DISCONNECTED)
        }

        fun fromSession(session: AuthSession): SessionSummaryUiModel {
            return SessionSummaryUiModel(
                connectionState = ConnectionState.CONNECTED,
                issuer = session.issuer,
                subject = session.subject,
                expiration = session.expiresAtIso,
            )
        }
    }
}

data class ProfileSettingsUiModel(
    val language: AppLanguage = AppLanguage.FR,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT_ONLY,
    val notificationsEnabled: Boolean = true,
)

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Error(
        val message: String,
        val settings: ProfileSettingsUiModel = ProfileSettingsUiModel(),
    ) : ProfileUiState

    data class Content(
        val role: UserRole,
        val profile: ProfileCardUiModel,
        val session: SessionSummaryUiModel,
        val settings: ProfileSettingsUiModel = ProfileSettingsUiModel(),
    ) : ProfileUiState
}
