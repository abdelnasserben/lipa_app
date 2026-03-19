package com.kori.app.feature.profile

import com.kori.app.core.model.UserRole

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
)

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
        val settings: ProfileSettingsUiModel = ProfileSettingsUiModel(),
    ) : ProfileUiState
}
