package com.kori.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.kori.app.app.di.KoriAppContainerFactory
import com.kori.app.app.di.RepositoryBindingMode
import com.kori.app.core.designsystem.KoriTheme
import com.kori.app.domain.GetDashboardUseCase
import com.kori.app.navigation.KoriNavHost

@Composable
fun KoriApp() {
    val context = LocalContext.current.applicationContext
    val appContainer = remember(context) {
        KoriAppContainerFactory.create(
            context = context,
            mode = RepositoryBindingMode.MOCK,
        )
    }

    val appState = remember(appContainer.sessionRepository) { KoriAppState(appContainer.sessionRepository) }
    val getDashboardUseCase = remember(appContainer.dashboardRepository) {
        GetDashboardUseCase(appContainer.dashboardRepository)
    }
    val navController = rememberNavController()

    KoriTheme {
        KoriNavHost(
            appState = appState,
            navController = navController,
            getDashboardUseCase = getDashboardUseCase,
            transactionRepository = appContainer.transactionRepository,
            clientCardRepository = appContainer.clientCardRepository,
            authService = appContainer.authService,
            clientTransferRepository = appContainer.clientTransferRepository,
            merchantTransferRepository = appContainer.merchantTransferRepository,
            agentActionRepository = appContainer.agentActionRepository,
            agentSearchRepository = appContainer.agentSearchRepository,
            activityRepository = appContainer.activityRepository,
            profileRepository = appContainer.profileRepository,
            localStorage = appContainer.localStorage,
            idempotencyManager = appContainer.idempotencyManager,
        )
    }
}
