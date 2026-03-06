package com.kori.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.UserRole
import com.kori.app.data.repository.ProfileRepository
import com.kori.app.data.repository.RoleProfilePayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val role: UserRole,
    private val repository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var settingsState = ProfileSettingsUiModel()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            runCatching {
                repository.getProfile(role)
            }.onSuccess { payload ->
                val profile = when (payload) {
                    is RoleProfilePayload.Client -> ProfileCardUiModel(
                        displayName = payload.value.displayName,
                        code = payload.value.code,
                        status = payload.value.status.name,
                        createdAt = payload.value.createdAt,
                        phone = payload.value.phone,
                    )

                    is RoleProfilePayload.Merchant -> ProfileCardUiModel(
                        displayName = payload.value.displayName,
                        code = payload.value.code,
                        status = payload.value.status.name,
                        createdAt = payload.value.createdAt,
                    )

                    is RoleProfilePayload.Agent -> ProfileCardUiModel(
                        displayName = payload.value.displayName,
                        code = payload.value.code,
                        status = payload.value.status.name,
                        createdAt = payload.value.createdAt,
                    )
                }

                _uiState.value = ProfileUiState.Content(
                    role = role,
                    profile = profile,
                    settings = settingsState,
                )
            }.onFailure {
                _uiState.value = ProfileUiState.Error(
                    message = "Impossible de charger le profil pour le moment.",
                    settings = settingsState,
                )
            }
        }
    }

    fun onLanguageSelected(language: AppLanguage) {
        settingsState = settingsState.copy(language = language)
        refreshSettingsOnly()
    }

    fun onNotificationsChanged(enabled: Boolean) {
        settingsState = settingsState.copy(notificationsEnabled = enabled)
        refreshSettingsOnly()
    }

    private fun refreshSettingsOnly() {
        val current = _uiState.value
        _uiState.value = when (current) {
            ProfileUiState.Loading -> current
            is ProfileUiState.Error -> current.copy(settings = settingsState)
            is ProfileUiState.Content -> current.copy(settings = settingsState)
        }
    }

    companion object {
        fun factory(
            role: UserRole,
            repository: ProfileRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(
                        role = role,
                        repository = repository,
                    ) as T
                }
            }
        }
    }
}