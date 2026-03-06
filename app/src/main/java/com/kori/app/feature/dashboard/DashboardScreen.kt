package com.kori.app.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurface
import com.kori.app.core.designsystem.component.BalanceCard
import com.kori.app.core.designsystem.component.EmptyState
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.designsystem.component.KPIWidget
import com.kori.app.core.designsystem.component.QuickActionGrid
import com.kori.app.core.designsystem.component.QuickActionIcon
import com.kori.app.core.designsystem.component.QuickActionItem
import com.kori.app.core.designsystem.component.SkeletonBalanceCard
import com.kori.app.core.designsystem.component.SkeletonInfoCard
import com.kori.app.core.designsystem.component.SkeletonTransactionRow
import com.kori.app.core.designsystem.component.TransactionRowCard
import com.kori.app.core.model.UserRole

@Composable
fun DashboardScreen(
    role: UserRole,
    uiState: DashboardUiState,
    onRetry: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        DashboardUiState.Loading -> DashboardLoading(modifier = modifier)
        DashboardUiState.Empty -> {
            DashboardEmpty(
                role = role,
                onOpenProfile = onOpenProfile,
                modifier = modifier,
            )
        }

        is DashboardUiState.Error -> {
            DashboardError(
                message = uiState.message,
                onRetry = onRetry,
                modifier = modifier,
            )
        }

        is DashboardUiState.Client -> {
            ClientDashboardContent(
                role = role,
                state = uiState,
                onOpenProfile = onOpenProfile,
                onOpenTransactions = onOpenTransactions,
                onOpenAction = onOpenAction,
                modifier = modifier,
            )
        }

        is DashboardUiState.Merchant -> {
            MerchantDashboardContent(
                role = role,
                state = uiState,
                onOpenTransactions = onOpenTransactions,
                onOpenAction = onOpenAction,
                modifier = modifier,
            )
        }

        is DashboardUiState.Agent -> {
            AgentDashboardContent(
                role = role,
                state = uiState,
                onOpenTransactions = onOpenTransactions,
                onOpenAction = onOpenAction,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun DashboardLoading(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Chargement du dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item { SkeletonBalanceCard() }
        item { SkeletonInfoCard() }
        items(3) { SkeletonTransactionRow() }
    }
}

@Composable
private fun DashboardEmpty(
    role: UserRole,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DashboardHeader(role = role)
        EmptyState(
            title = "Aucune donnée disponible",
            message = "Votre espace sera alimenté dès les premières activités.",
            actionLabel = "Voir le profil",
            onActionClick = onOpenProfile,
        )
    }
}

@Composable
private fun DashboardError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        ErrorState(
            title = "Chargement indisponible",
            message = message,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun ClientDashboardContent(
    role: UserRole,
    state: DashboardUiState.Client,
    onOpenProfile: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DashboardHeader(role = role) }

        item {
            BalanceCard(
                title = "Solde principal",
                balance = state.data.balance,
            )
        }

        item {
            QuickActionGrid(
                items = listOf(
                    QuickActionItem(
                        title = "Envoyer",
                        icon = QuickActionIcon.SEND,
                        onClick = onOpenAction,
                    ),
                    QuickActionItem(
                        title = "Historique",
                        icon = QuickActionIcon.HISTORY,
                        onClick = onOpenTransactions,
                    ),
                    QuickActionItem(
                        title = "Cartes",
                        icon = QuickActionIcon.CARD,
                        onClick = onOpenProfile,
                    ),
                ),
            )
        }

        if (state.data.cards.isNotEmpty()) {
            item {
                AlertCard(
                    title = "Cartes",
                    messages = state.data.cards.map {
                        "${it.cardUid} • ${it.status}"
                    },
                )
            }
        }

        if (state.data.alerts.isNotEmpty()) {
            item {
                AlertCard(
                    title = "Alertes",
                    messages = state.data.alerts.map { it.message },
                )
            }
        }

        item {
            SectionTitle("Transactions récentes")
        }

        items(state.data.recentTransactions) { transaction ->
            TransactionRowCard(transaction = transaction)
        }

        item {
            Button(
                onClick = onOpenProfile,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KoriAccent,
                    contentColor = KoriPrimary,
                ),
            ) {
                Text("Voir mon profil")
            }
        }
    }
}

@Composable
private fun MerchantDashboardContent(
    role: UserRole,
    state: DashboardUiState.Merchant,
    onOpenTransactions: () -> Unit,
    onOpenAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DashboardHeader(role = role) }

        item {
            BalanceCard(
                title = "Trésorerie disponible",
                balance = state.data.balance,
            )
        }

        item {
            KPIWidget(
                title = "Performance sur 7 jours",
                txCount = state.data.kpis7d.txCount,
                txVolume = state.data.kpis7d.txVolume,
                failedCount = state.data.kpis7d.failedCount,
                chartPoints = listOf(18f, 24f, 20f, 28f, 34f, 30f, 38f),
            )
        }

        item {
            QuickActionGrid(
                items = listOf(
                    QuickActionItem(
                        title = "Transférer",
                        icon = QuickActionIcon.TRANSFER,
                        onClick = onOpenAction,
                    ),
                    QuickActionItem(
                        title = "Historique",
                        icon = QuickActionIcon.HISTORY,
                        onClick = onOpenTransactions,
                    ),
                    QuickActionItem(
                        title = "Solde",
                        icon = QuickActionIcon.WALLET,
                        onClick = onOpenTransactions,
                    ),
                ),
            )
        }

        item {
            AlertCard(
                title = "Terminaux",
                messages = buildList {
                    add("Total : ${state.data.terminalsSummary.total}")
                    add("Terminaux hors ligne : ${state.data.terminalsSummary.staleTerminals}")
                    state.data.terminalsSummary.byStatus.forEach { (status, count) ->
                        add("$status : $count")
                    }
                },
            )
        }

        item {
            SectionTitle("Transactions récentes")
        }

        items(state.data.recentTransactions) { transaction ->
            TransactionRowCard(transaction = transaction)
        }
    }
}

@Composable
private fun AgentDashboardContent(
    role: UserRole,
    state: DashboardUiState.Agent,
    onOpenTransactions: () -> Unit,
    onOpenAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DashboardHeader(role = role) }

        item {
            BalanceCard(
                title = "Position agent",
                balance = state.data.balance,
            )
        }

        item {
            KPIWidget(
                title = "Activité sur 7 jours",
                txCount = state.data.kpis7d.txCount,
                txVolume = state.data.kpis7d.txVolume,
                failedCount = state.data.kpis7d.failedCount,
                chartPoints = listOf(10f, 14f, 13f, 19f, 16f, 22f, 24f),
            )
        }

        item {
            QuickActionGrid(
                items = listOf(
                    QuickActionItem(
                        title = "Cash-in",
                        icon = QuickActionIcon.WALLET,
                        onClick = onOpenAction,
                    ),
                    QuickActionItem(
                        title = "Retraits",
                        icon = QuickActionIcon.TRANSFER,
                        onClick = onOpenAction,
                    ),
                    QuickActionItem(
                        title = "Historique",
                        icon = QuickActionIcon.HISTORY,
                        onClick = onOpenTransactions,
                    ),
                ),
            )
        }

        if (state.data.alerts.isNotEmpty()) {
            item {
                AlertCard(
                    title = "Alertes",
                    messages = state.data.alerts.map { it.message },
                )
            }
        }

        item {
            SectionTitle("Transactions récentes")
        }

        items(state.data.recentTransactions) { transaction ->
            TransactionRowCard(transaction = transaction)
        }

        item {
            SectionTitle("Activités récentes")
        }

        items(state.data.recentActivities) { activity ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = KoriSurface),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = activity.action,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = activity.resourceRef,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = activity.occurredAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    role: UserRole,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = role.dashboardTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = KoriPrimary,
        )
        Text(
            text = "Un aperçu clair et rassurant de votre activité KORI.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun AlertCard(
    title: String,
    messages: List<String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            messages.forEach { message ->
                Text(
                    text = "• $message",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}