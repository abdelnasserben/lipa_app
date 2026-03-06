package com.kori.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.UserRole
import com.kori.app.domain.DashboardPayload
import com.kori.app.domain.GetDashboardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val role: UserRole,
    private val getDashboardUseCase: GetDashboardUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            _uiState.value = runCatching {
                getDashboardUseCase(role)
            }.fold(
                onSuccess = { payload ->
                    when (payload) {
                        is DashboardPayload.Client -> {
                            if (
                                payload.value.cards.isEmpty() &&
                                payload.value.recentTransactions.isEmpty() &&
                                payload.value.alerts.isEmpty()
                            ) {
                                DashboardUiState.Empty
                            } else {
                                DashboardUiState.Client(payload.value)
                            }
                        }

                        is DashboardPayload.Merchant -> {
                            if (payload.value.recentTransactions.isEmpty()) {
                                DashboardUiState.Empty
                            } else {
                                DashboardUiState.Merchant(payload.value)
                            }
                        }

                        is DashboardPayload.Agent -> {
                            if (
                                payload.value.recentTransactions.isEmpty() &&
                                payload.value.recentActivities.isEmpty() &&
                                payload.value.alerts.isEmpty()
                            ) {
                                DashboardUiState.Empty
                            } else {
                                DashboardUiState.Agent(payload.value)
                            }
                        }
                    }
                },
                onFailure = {
                    DashboardUiState.Error(
                        message = "Impossible de charger le dashboard pour le moment.",
                    )
                },
            )
        }
    }

    companion object {
        fun factory(
            role: UserRole,
            getDashboardUseCase: GetDashboardUseCase,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(
                        role = role,
                        getDashboardUseCase = getDashboardUseCase,
                    ) as T
                }
            }
        }
    }
}