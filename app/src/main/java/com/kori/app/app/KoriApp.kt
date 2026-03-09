package com.kori.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.kori.app.core.designsystem.KoriTheme
import com.kori.app.data.idempotency.InMemoryPendingActionStore
import com.kori.app.data.local.SharedPrefsLocalStorage
import com.kori.app.data.mock.MockActivityRepository
import com.kori.app.data.mock.MockAgentActionRepository
import com.kori.app.data.mock.MockAgentSearchRepository
import com.kori.app.data.mock.MockAuthService
import com.kori.app.data.mock.MockClientCardRepository
import com.kori.app.data.mock.MockClientTransferRepository
import com.kori.app.data.mock.MockDashboardRepository
import com.kori.app.data.mock.MockMerchantTransferRepository
import com.kori.app.data.mock.MockProfileRepository
import com.kori.app.data.mock.MockSessionRepository
import com.kori.app.data.mock.MockTransactionRepository
import com.kori.app.domain.GetDashboardUseCase
import com.kori.app.domain.idempotency.IdempotencyManager
import com.kori.app.navigation.KoriNavHost

@Composable
fun KoriApp() {
    val context = LocalContext.current.applicationContext
    val localStorage = remember(context) { SharedPrefsLocalStorage(context) }

    val sessionRepository = remember(localStorage) { MockSessionRepository(localStorage) }
    val dashboardRepository = remember { MockDashboardRepository() }
    val transactionRepository = remember { MockTransactionRepository() }
    val clientCardRepository = remember { MockClientCardRepository() }
    val authService = remember(localStorage) { MockAuthService(localStorage) }
    val activityRepository = remember { MockActivityRepository() }
    val clientTransferRepository = remember { MockClientTransferRepository() }
    val merchantTransferRepository = remember { MockMerchantTransferRepository() }
    val agentActionRepository = remember { MockAgentActionRepository() }
    val agentSearchRepository = remember { MockAgentSearchRepository() }
    val profileRepository = remember { MockProfileRepository() }
    val pendingActionStore = remember { InMemoryPendingActionStore() }
    val idempotencyManager = remember(pendingActionStore) { IdempotencyManager(pendingActionStore) }

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
            clientCardRepository = clientCardRepository,
            authService = authService,
            clientTransferRepository = clientTransferRepository,
            merchantTransferRepository = merchantTransferRepository,
            agentActionRepository = agentActionRepository,
            agentSearchRepository = agentSearchRepository,
            activityRepository = activityRepository,
            profileRepository = profileRepository,
            localStorage = localStorage,
            idempotencyManager = idempotencyManager,
        )
    }
}
