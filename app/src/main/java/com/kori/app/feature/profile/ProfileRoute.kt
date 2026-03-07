package com.kori.app.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.auth.AuthState
import com.kori.app.data.local.LocalStorage
import com.kori.app.data.repository.ProfileRepository

@Composable
fun ProfileRoute(
    role: UserRole,
    authState: AuthState,
    repository: ProfileRepository,
    localStorage: LocalStorage,
    onOpenSession: () -> Unit,
    onSelectRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(
            role = role,
            repository = repository,
            localStorage = localStorage,
        ),
    )

    LaunchedEffect(authState) {
        viewModel.onAuthStateChanged(authState)
    }

    val uiState by viewModel.uiState.collectAsState()

    ProfileScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        onOpenSession = onOpenSession,
        onLanguageSelected = viewModel::onLanguageSelected,
        onNotificationsChanged = viewModel::onNotificationsChanged,
        onSelectRole = onSelectRole,
        modifier = modifier,
    )
}
