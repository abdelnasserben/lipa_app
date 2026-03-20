package com.kori.app.feature.profile

import androidx.annotation.StringRes
import com.kori.app.R
import com.kori.app.core.model.UserRole

enum class AppLanguage(
    @StringRes val labelResId: Int,
) {
    FR(R.string.profile_language_french),
    EN(R.string.profile_language_english);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.name == code } ?: FR
        }
    }
}

enum class AppThemeMode(
    @StringRes val labelResId: Int,
) {
    LIGHT_ONLY(R.string.profile_theme_light_only),
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
