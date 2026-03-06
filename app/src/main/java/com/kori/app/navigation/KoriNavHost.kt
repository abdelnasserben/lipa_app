package com.kori.app.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kori.app.app.KoriAppState
import com.kori.app.core.designsystem.component.KoriBottomBar
import com.kori.app.core.designsystem.component.KoriScaffold
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.auth.AuthState
import com.kori.app.data.repository.AgentActionRepository
import com.kori.app.data.repository.AuthService
import com.kori.app.data.repository.ClientTransferRepository
import com.kori.app.data.repository.MerchantTransferRepository
import com.kori.app.data.repository.ProfileRepository
import com.kori.app.data.repository.TransactionRepository
import com.kori.app.domain.GetDashboardUseCase
import com.kori.app.feature.action.ActionScreen
import com.kori.app.feature.action.AgentCashInRoute
import com.kori.app.feature.action.AgentMerchantWithdrawRoute
import com.kori.app.feature.action.ClientTransferRoute
import com.kori.app.feature.action.MerchantTransferRoute
import com.kori.app.feature.activity.ActivityScreen
import com.kori.app.feature.auth.AuthBrowserMockScreen
import com.kori.app.feature.auth.AuthCallbackScreen
import com.kori.app.feature.auth.AuthSuccessScreen
import com.kori.app.feature.auth.AuthViewModel
import com.kori.app.feature.auth.AuthWelcomeScreen
import com.kori.app.feature.dashboard.DashboardRoute
import com.kori.app.feature.profile.ProfileRoute
import com.kori.app.feature.profile.SessionScreen
import com.kori.app.feature.rolepicker.RolePickerScreen
import com.kori.app.feature.transactions.TransactionDetailRoute
import com.kori.app.feature.transactions.TransactionsRoute

@Composable
fun KoriNavHost(
    appState: KoriAppState,
    navController: NavHostController,
    getDashboardUseCase: GetDashboardUseCase,
    transactionRepository: TransactionRepository,
    authService: AuthService,
    clientTransferRepository: ClientTransferRepository,
    merchantTransferRepository: MerchantTransferRepository,
    agentActionRepository: AgentActionRepository,
    profileRepository: ProfileRepository,
    modifier: Modifier = Modifier,
) {
    val session by appState.session.collectAsState()
    val role = session.selectedRole
    val authState by authService.authState.collectAsState()

    val startDestination = when {
        !session.isRoleSelected -> KoriDestination.RolePicker.route
        authState is AuthState.Authenticated -> KoriDestination.Dashboard.route
        else -> KoriDestination.AuthWelcome.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(KoriDestination.RolePicker.route) {
            RolePickerScreen(
                onRoleSelected = { selectedRole ->
                    appState.selectRole(selectedRole)
                    val nextRoute = when (authState) {
                        is AuthState.Authenticated -> KoriDestination.Dashboard.route
                        else -> KoriDestination.AuthWelcome.route
                    }

                    navController.navigate(nextRoute) {
                        popUpTo(KoriDestination.RolePicker.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(KoriDestination.AuthWelcome.route) {
            if (role == null) return@composable

            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.factory(authService),
            )

            AuthWelcomeScreen(
                role = role,
                onLoginClick = {
                    authViewModel.beginLogin()
                    navController.navigate(KoriDestination.AuthBrowserMock.route)
                },
            )
        }

        composable(KoriDestination.AuthBrowserMock.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.factory(authService),
            )

            AuthBrowserMockScreen(
                onContinueClick = {
                    navController.navigate(KoriDestination.AuthCallback.route)
                },
                onErrorClick = {
                    authViewModel.failLogin("La connexion Keycloak mock a été interrompue.")
                    navController.navigate(KoriDestination.AuthCallback.route)
                },
            )
        }

        composable(KoriDestination.AuthCallback.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.factory(authService),
            )
            val localAuthState by authViewModel.authState.collectAsState()

            AuthCallbackScreen(
                authState = localAuthState,
                onProcess = {
                    if (localAuthState !is AuthState.Error && localAuthState !is AuthState.Authenticated) {
                        authViewModel.completeLogin()
                    }
                },
                onSuccess = {
                    navController.navigate(KoriDestination.AuthSuccess.route) {
                        popUpTo(KoriDestination.AuthWelcome.route)
                    }
                },
                onRetry = {
                    authViewModel.beginLogin()
                    navController.navigate(KoriDestination.AuthBrowserMock.route) {
                        popUpTo(KoriDestination.AuthWelcome.route)
                    }
                },
            )
        }

        composable(KoriDestination.AuthSuccess.route) {
            AuthSuccessScreen(
                onContinue = {
                    navController.navigate(KoriDestination.Dashboard.route) {
                        popUpTo(KoriDestination.AuthWelcome.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(KoriDestination.Dashboard.route) {
            if (role == null) return@composable
            if (authState !is AuthState.Authenticated) {
                LaunchedEffect(authState) {
                    navController.navigate(KoriDestination.AuthWelcome.route) {
                        popUpTo(KoriDestination.Dashboard.route) {
                            inclusive = true
                        }
                    }
                }
                return@composable
            }

            KoriScaffold(
                bottomBar = {
                    KoriBottomBar(
                        currentRoute = KoriDestination.Dashboard.route,
                        activityLabel = role.historyLabel,
                        actionLabel = role.actionLabel,
                        onNavigate = { route -> navController.navigateToTopLevel(route) },
                    )
                },
            ) { _ ->
                DashboardRoute(
                    role = role,
                    getDashboardUseCase = getDashboardUseCase,
                    onOpenProfile = {
                        navController.navigateToTopLevel(KoriDestination.Profile.route)
                    },
                    onOpenTransactions = {
                        navController.navigateToTopLevel(KoriDestination.Transactions.route)
                    },
                    onOpenAction = {
                        navController.navigateToTopLevel(KoriDestination.Action.route)
                    },
                )
            }
        }

        composable(KoriDestination.Transactions.route) {
            if (role == null) return@composable
            if (authState !is AuthState.Authenticated) return@composable

            KoriScaffold(
                bottomBar = {
                    KoriBottomBar(
                        currentRoute = KoriDestination.Transactions.route,
                        activityLabel = role.historyLabel,
                        actionLabel = role.actionLabel,
                        onNavigate = { route -> navController.navigateToTopLevel(route) },
                    )
                },
            ) { _ ->
                TransactionsRoute(
                    role = role,
                    repository = transactionRepository,
                    onTransactionClick = { transactionRef ->
                        navController.navigate(
                            KoriDestination.TransactionDetail.createRoute(transactionRef),
                        )
                    },
                )
            }
        }

        composable(
            route = KoriDestination.TransactionDetail.route,
            arguments = listOf(
                navArgument("transactionRef") {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            if (role == null) return@composable
            if (authState !is AuthState.Authenticated) return@composable

            val transactionRef = backStackEntry.arguments?.getString("transactionRef").orEmpty()

            TopBarPage(
                title = "Détail transaction",
                onBack = { navController.popBackStack() },
            ) { _ ->
                TransactionDetailRoute(
                    role = role,
                    transactionRef = transactionRef,
                    repository = transactionRepository,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        composable(KoriDestination.ClientTransfer.route) {
            if (role != UserRole.CLIENT) return@composable
            if (authState !is AuthState.Authenticated) return@composable

            TopBarPage(
                title = "Transfert client",
                onBack = { navController.popBackStack() },
            ) { _ ->
                ClientTransferRoute(repository = clientTransferRepository)
            }
        }

        composable(KoriDestination.MerchantTransfer.route) {
            if (role != UserRole.MERCHANT) return@composable
            if (authState !is AuthState.Authenticated) return@composable

            TopBarPage(
                title = "Transfert marchand",
                onBack = { navController.popBackStack() },
            ) { _ ->
                MerchantTransferRoute(repository = merchantTransferRepository)
            }
        }

        composable(KoriDestination.AgentCashIn.route) {
            if (role != UserRole.AGENT) return@composable
            if (authState !is AuthState.Authenticated) return@composable

            TopBarPage(
                title = "Cash-in client",
                onBack = { navController.popBackStack() },
            ) { _ ->
                AgentCashInRoute(repository = agentActionRepository)
            }
        }

        composable(KoriDestination.AgentMerchantWithdraw.route) {
            if (role != UserRole.AGENT) return@composable
            if (authState !is AuthState.Authenticated) return@composable

            TopBarPage(
                title = "Retrait marchand",
                onBack = { navController.popBackStack() },
            ) { _ ->
                AgentMerchantWithdrawRoute(repository = agentActionRepository)
            }
        }

        composable(KoriDestination.Action.route) {
            if (role == null) return@composable
            if (authState !is AuthState.Authenticated) return@composable

            KoriScaffold(
                bottomBar = {
                    KoriBottomBar(
                        currentRoute = KoriDestination.Action.route,
                        activityLabel = role.historyLabel,
                        actionLabel = role.actionLabel,
                        onNavigate = { route -> navController.navigateToTopLevel(route) },
                    )
                },
            ) { contentModifier ->
                ActionScreen(
                    role = role,
                    onOpenClientTransfer = {
                        navController.navigate(KoriDestination.ClientTransfer.route)
                    },
                    onOpenMerchantTransfer = {
                        navController.navigate(KoriDestination.MerchantTransfer.route)
                    },
                    onOpenAgentCashIn = {
                        navController.navigate(KoriDestination.AgentCashIn.route)
                    },
                    onOpenAgentMerchantWithdraw = {
                        navController.navigate(KoriDestination.AgentMerchantWithdraw.route)
                    },
                    modifier = contentModifier,
                )
            }
        }

        composable(KoriDestination.Activity.route) {
            if (role == null) return@composable
            if (authState !is AuthState.Authenticated) return@composable

            KoriScaffold(
                bottomBar = {
                    KoriBottomBar(
                        currentRoute = KoriDestination.Activity.route,
                        activityLabel = role.historyLabel,
                        actionLabel = role.actionLabel,
                        onNavigate = { route -> navController.navigateToTopLevel(route) },
                    )
                },
            ) { contentModifier ->
                ActivityScreen(
                    role = role,
                    modifier = contentModifier,
                )
            }
        }

        composable(KoriDestination.Profile.route) {
            if (role == null) return@composable
            if (authState !is AuthState.Authenticated) return@composable

            KoriScaffold(
                bottomBar = {
                    KoriBottomBar(
                        currentRoute = KoriDestination.Profile.route,
                        activityLabel = role.historyLabel,
                        actionLabel = role.actionLabel,
                        onNavigate = { route -> navController.navigateToTopLevel(route) },
                    )
                },
            ) { _ ->
                ProfileRoute(
                    role = role,
                    repository = profileRepository,
                    onOpenSession = {
                        navController.navigate(KoriDestination.Session.route)
                    },
                    onSelectRole = { selectedRole ->
                        appState.switchRole(selectedRole)
                        navController.navigate(KoriDestination.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        composable(KoriDestination.Session.route) {
            val currentState by authService.authState.collectAsState()

            if (currentState !is AuthState.Authenticated) return@composable

            TopBarPage(
                title = "Session OIDC",
                onBack = { navController.popBackStack() },
            ) { contentModifier ->
                SessionScreen(
                    session = (currentState as AuthState.Authenticated).session,
                    onLogout = {
                        authService.logout()
                        navController.navigate(KoriDestination.AuthWelcome.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                        }
                    },
                    modifier = contentModifier,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarPage(
    title: String,
    onBack: () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing.union(WindowInsets.navigationBars),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Retour",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        content(
            Modifier.padding(innerPadding),
        )
    }
}

private fun NavHostController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}