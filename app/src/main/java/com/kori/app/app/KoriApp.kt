package com.kori.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.kori.app.core.designsystem.KoriTheme
import com.kori.app.data.mock.MockAgentActionRepository
import com.kori.app.data.mock.MockAuthService
import com.kori.app.data.mock.MockClientTransferRepository
import com.kori.app.data.mock.MockDashboardRepository
import com.kori.app.data.mock.MockMerchantTransferRepository
import com.kori.app.data.mock.MockProfileRepository
import com.kori.app.data.mock.MockSessionRepository
import com.kori.app.data.mock.MockTransactionRepository
import com.kori.app.domain.GetDashboardUseCase
import com.kori.app.navigation.KoriNavHost

@Composable
fun KoriApp() {
    val sessionRepository = remember { MockSessionRepository() }
    val dashboardRepository = remember { MockDashboardRepository() }
    val transactionRepository = remember { MockTransactionRepository() }
    val authService = remember { MockAuthService() }
    val clientTransferRepository = remember { MockClientTransferRepository() }
    val merchantTransferRepository = remember { MockMerchantTransferRepository() }
    val agentActionRepository = remember { MockAgentActionRepository() }
    val profileRepository = remember { MockProfileRepository() }

    val appState = remember(sessionRepository) { KoriAppState(sessionRepository) }
    val getDashboardUseCase = remember(dashboardRepository) {
        GetDashboardUseCase(dashboardRepository)
    }
    val navController = rememberNavController()

    KoriTheme {
        KoriNavHost(
            appState = appState,
            navController = navController,
            getDashboardUseCase = getDashboardUseCase,
            transactionRepository = transactionRepository,
            authService = authService,
            clientTransferRepository = clientTransferRepository,
            merchantTransferRepository = merchantTransferRepository,
            agentActionRepository = agentActionRepository,
            profileRepository = profileRepository,
        )
    }
}