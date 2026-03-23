package com.kori.app.app.di

import android.content.Context
import com.kori.app.data.idempotency.InMemoryPendingActionStore
import com.kori.app.data.local.LocalStorage
import com.kori.app.data.local.SharedPrefsLocalStorage
import com.kori.app.data.mock.MockActivityRepository
import com.kori.app.data.mock.MockAgentActionDataSource
import com.kori.app.data.mock.MockAgentActionRepository
import com.kori.app.data.mock.MockAgentSearchRepository
import com.kori.app.data.mock.MockClientCardRepository
import com.kori.app.data.mock.MockClientTransferDataSource
import com.kori.app.data.mock.MockClientTransferRepository
import com.kori.app.data.mock.MockDashboardDataSource
import com.kori.app.data.mock.MockDashboardRepository
import com.kori.app.data.mock.MockMerchantTransferDataSource
import com.kori.app.data.mock.MockMerchantTransferRepository
import com.kori.app.data.mock.MockProfileDataSource
import com.kori.app.data.mock.MockProfileRepository
import com.kori.app.data.mock.MockTransactionDataSource
import com.kori.app.data.mock.MockTransactionRepository
import com.kori.app.data.oidc.OidcAuthDataSource
import com.kori.app.data.oidc.OidcConfig
import com.kori.app.data.remote.ApiHttpClient
import com.kori.app.data.remote.NetworkBackedAgentActionDataSource
import com.kori.app.data.remote.NetworkBackedClientTransferDataSource
import com.kori.app.data.remote.NetworkBackedMerchantTransferDataSource
import com.kori.app.data.remote.RealDashboardDataSource
import com.kori.app.data.remote.RealProfileDataSource
import com.kori.app.data.remote.RealTransactionDataSource
import com.kori.app.data.repository.ActivityRepository
import com.kori.app.data.repository.AgentActionRepository
import com.kori.app.data.repository.AgentSearchRepository
import com.kori.app.data.repository.AuthService
import com.kori.app.data.repository.ClientCardRepository
import com.kori.app.data.repository.ClientTransferRepository
import com.kori.app.data.repository.DashboardRepository
import com.kori.app.data.repository.MerchantTransferRepository
import com.kori.app.data.repository.ProfileRepository
import com.kori.app.data.repository.SessionRepository
import com.kori.app.data.repository.TransactionRepository
import com.kori.app.data.repository.impl.AgentActionRepositoryImpl
import com.kori.app.data.repository.impl.AuthServiceImpl
import com.kori.app.data.repository.impl.ClientTransferRepositoryImpl
import com.kori.app.data.repository.impl.DashboardRepositoryImpl
import com.kori.app.data.repository.impl.MerchantTransferRepositoryImpl
import com.kori.app.data.repository.impl.ProfileRepositoryImpl
import com.kori.app.data.repository.impl.SessionRepositoryImpl
import com.kori.app.data.repository.impl.TransactionRepositoryImpl
import com.kori.app.domain.idempotency.IdempotencyManager

interface KoriAppContainer {
    val localStorage: LocalStorage
    val sessionRepository: SessionRepository
    val dashboardRepository: DashboardRepository
    val transactionRepository: TransactionRepository
    val clientCardRepository: ClientCardRepository
    val authService: AuthService
    val activityRepository: ActivityRepository
    val clientTransferRepository: ClientTransferRepository
    val merchantTransferRepository: MerchantTransferRepository
    val agentActionRepository: AgentActionRepository
    val agentSearchRepository: AgentSearchRepository
    val profileRepository: ProfileRepository
    val idempotencyManager: IdempotencyManager
}

object KoriAppContainerFactory {
    fun create(context: Context, mode: RepositoryBindingMode = RepositoryBindingMode.REAL): KoriAppContainer =
        when (mode) {
            RepositoryBindingMode.MOCK -> MockKoriAppContainer(context)
            RepositoryBindingMode.REAL -> RealKoriAppContainer(context)
        }
}

enum class RepositoryBindingMode {
    MOCK,
    REAL,
}

private abstract class BaseKoriAppContainer(context: Context) : KoriAppContainer {
    override val localStorage: LocalStorage = SharedPrefsLocalStorage(context)

    override val authService: AuthService = AuthServiceImpl(
        OidcAuthDataSource(
            context = context,
            localStorage = localStorage,
            oidcConfig = OidcConfig.fromBuildConfig(),
        ),
    )

    override val sessionRepository: SessionRepository = SessionRepositoryImpl(
        localStorage = localStorage,
        authService = authService,
    )

    override val clientCardRepository: ClientCardRepository = MockClientCardRepository()
    override val activityRepository: ActivityRepository = MockActivityRepository()
    override val agentSearchRepository: AgentSearchRepository = MockAgentSearchRepository()

    protected val pendingActionStore = InMemoryPendingActionStore()
    override val idempotencyManager: IdempotencyManager = IdempotencyManager(pendingActionStore)
}

private class MockKoriAppContainer(context: Context) : BaseKoriAppContainer(context) {
    private val mockDashboardRepository = MockDashboardRepository()
    private val mockTransactionRepository = MockTransactionRepository()
    private val mockProfileRepository = MockProfileRepository()
    private val mockClientTransferRepository = MockClientTransferRepository()
    private val mockMerchantTransferRepository = MockMerchantTransferRepository()
    private val mockAgentActionRepository = MockAgentActionRepository()

    override val dashboardRepository: DashboardRepository =
        DashboardRepositoryImpl(MockDashboardDataSource(mockDashboardRepository))
    override val transactionRepository: TransactionRepository =
        TransactionRepositoryImpl(MockTransactionDataSource(mockTransactionRepository))
    override val clientTransferRepository: ClientTransferRepository =
        ClientTransferRepositoryImpl(MockClientTransferDataSource(mockClientTransferRepository))
    override val merchantTransferRepository: MerchantTransferRepository =
        MerchantTransferRepositoryImpl(MockMerchantTransferDataSource(mockMerchantTransferRepository))
    override val agentActionRepository: AgentActionRepository =
        AgentActionRepositoryImpl(MockAgentActionDataSource(mockAgentActionRepository))
    override val profileRepository: ProfileRepository =
        ProfileRepositoryImpl(MockProfileDataSource(mockProfileRepository))
}

private class RealKoriAppContainer(context: Context) : BaseKoriAppContainer(context) {
    private val apiHttpClient = ApiHttpClient(authService = authService)

    private val fallbackAgentActionRepository = MockAgentActionRepository()
    private val fallbackAgentActionDataSource = MockAgentActionDataSource(fallbackAgentActionRepository)

    override val dashboardRepository: DashboardRepository =
        DashboardRepositoryImpl(RealDashboardDataSource(apiHttpClient))
    override val transactionRepository: TransactionRepository =
        TransactionRepositoryImpl(RealTransactionDataSource(apiHttpClient))
    override val clientTransferRepository: ClientTransferRepository =
        ClientTransferRepositoryImpl(NetworkBackedClientTransferDataSource(apiHttpClient))
    override val merchantTransferRepository: MerchantTransferRepository =
        MerchantTransferRepositoryImpl(NetworkBackedMerchantTransferDataSource(apiHttpClient))
    override val agentActionRepository: AgentActionRepository =
        AgentActionRepositoryImpl(NetworkBackedAgentActionDataSource(apiHttpClient, fallbackAgentActionDataSource))
    override val profileRepository: ProfileRepository =
        ProfileRepositoryImpl(RealProfileDataSource(apiHttpClient))
}
