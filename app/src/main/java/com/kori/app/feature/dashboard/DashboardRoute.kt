package com.kori.app.feature.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.core.model.UserRole
import com.kori.app.domain.GetDashboardUseCase

@Composable
fun DashboardRoute(
    role: UserRole,
    getDashboardUseCase: GetDashboardUseCase,
    onOpenProfile: () -> Unit,
    onOpenCards: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(
            role = role,
            getDashboardUseCase = getDashboardUseCase,
        ),
    )

    val uiState by viewModel.uiState.collectAsState()

    DashboardScreen(
        role = role,
        uiState = uiState,
        onRetry = viewModel::load,
        onOpenProfile = onOpenProfile,
        onOpenCards = onOpenCards,
        onOpenTransactions = onOpenTransactions,
        onOpenAction = onOpenAction,
        modifier = modifier
    )
}