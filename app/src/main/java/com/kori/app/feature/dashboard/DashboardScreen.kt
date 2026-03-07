package com.kori.app.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurface
import com.kori.app.core.designsystem.KoriSurfaceVariant
import com.kori.app.core.designsystem.component.BalanceCard
import com.kori.app.core.designsystem.component.ClientCardListItem
import com.kori.app.core.designsystem.component.EmptyState
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.designsystem.component.KPIWidget
import com.kori.app.core.designsystem.component.QuickActionGrid
import com.kori.app.core.designsystem.component.QuickActionIcon
import com.kori.app.core.designsystem.component.QuickActionItem
import com.kori.app.core.designsystem.component.SkeletonBalanceCard
import com.kori.app.core.designsystem.component.SkeletonInfoCard
import com.kori.app.core.designsystem.component.SkeletonTransactionRow
import com.kori.app.core.designsystem.component.StatusBadge
import com.kori.app.core.designsystem.component.TransactionRowCard
import com.kori.app.core.designsystem.component.TypeChip
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.balance.ActorBalanceResponse
import com.kori.app.core.model.common.BalanceKind
import com.kori.app.core.model.dashboard.ActivityItem
import com.kori.app.core.model.dashboard.AlertItem
import com.kori.app.core.model.dashboard.CardItem
import com.kori.app.core.model.dashboard.Kpis7dResponse
import com.kori.app.core.model.dashboard.TerminalsSummaryResponse
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.core.ui.formatIsoToDisplay
import com.kori.app.core.ui.formatKmf

@Composable
fun DashboardScreen(
    role: UserRole,
    uiState: DashboardUiState,
    onRetry: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCards: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        DashboardUiState.Loading -> DashboardLoading(
            role = role,
            modifier = modifier,
        )

        DashboardUiState.Empty -> {
            DashboardEmpty(
                role = role,
                onOpenProfile = onOpenProfile,
                onOpenTransactions = onOpenTransactions,
                onOpenAction = onOpenAction,
                modifier = modifier,
            )
        }

        is DashboardUiState.Error -> {
            DashboardError(
                role = role,
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
                onOpenCards = onOpenCards,
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
    role: UserRole,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 120.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DashboardHeader(role = role) }

        item {
            RoleHintCard(
                title = when (role) {
                    UserRole.CLIENT -> "Préparation de votre espace client"
                    UserRole.MERCHANT -> "Préparation du cockpit marchand"
                    UserRole.AGENT -> "Préparation de votre poste agent"
                },
                message = when (role) {
                    UserRole.CLIENT -> "Solde, cartes, alertes et dernières transactions arrivent."
                    UserRole.MERCHANT -> "KPIs 7 jours, terminaux et activité récente arrivent."
                    UserRole.AGENT -> "Cash, commission, alertes et activités récentes arrivent."
                },
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
    onOpenTransactions: () -> Unit,
    onOpenAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DashboardHeader(role = role)

        when (role) {
            UserRole.CLIENT -> {
                EmptyState(
                    title = "Votre espace client est prêt",
                    message = "Dès vos premiers mouvements, vous verrez ici vos cartes, alertes et transactions récentes.",
                    actionLabel = "Voir mon profil",
                    onActionClick = onOpenProfile,
                )
            }

            UserRole.MERCHANT -> {
                EmptyState(
                    title = "Aucune activité marchande récente",
                    message = "Les indicateurs 7 jours et le résumé des terminaux apparaîtront dès les premières transactions.",
                    actionLabel = "Voir l'historique",
                    onActionClick = onOpenTransactions,
                )
            }

            UserRole.AGENT -> {
                EmptyState(
                    title = "Aucune activité agent pour le moment",
                    message = "Votre position cash, votre commission et vos dernières opérations apparaîtront ici après vos premiers mouvements.",
                    actionLabel = "Démarrer une opération",
                    onActionClick = onOpenAction,
                )
            }

            else -> {
                EmptyState(
                    title = "Aucune donnée disponible",
                    message = "Votre dashboard se remplira dès les premières activités.",
                    actionLabel = "Voir le profil",
                    onActionClick = onOpenProfile,
                )
            }
        }
    }
}

@Composable
private fun DashboardError(
    role: UserRole,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DashboardHeader(role = role)

        ErrorState(
            title = when (role) {
                UserRole.CLIENT -> "Dashboard client indisponible"
                UserRole.MERCHANT -> "Dashboard marchand indisponible"
                UserRole.AGENT -> "Dashboard agent indisponible"
                else -> "Dashboard indisponible"
            },
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
    onOpenCards: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 120.dp,
        ),
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
            SectionBlock(
                eyebrow = "Actions rapides",
                title = "Accès direct à l'essentiel",
                subtitle = "Vos actions client les plus utiles en un coup d'œil.",
            ) {
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
                            onClick = onOpenCards,
                        ),
                    ),
                )
            }
        }

        if (state.data.alerts.isNotEmpty()) {
            item {
                AlertsSection(
                    title = "Alertes importantes",
                    subtitle = "Points d'attention à traiter rapidement.",
                    alerts = state.data.alerts,
                )
            }
        }

        if (state.data.cards.isNotEmpty()) {
            item {
                CardsPreviewSection(
                    cards = state.data.cards.take(5),
                    onOpenCards = onOpenCards,
                )
            }
        }

        item {
            TransactionsSection(
                title = "Transactions récentes",
                subtitle = "Vos derniers mouvements visibles immédiatement.",
                transactions = state.data.recentTransactions,
            )
        }

        item {
            Button(
                onClick = onOpenProfile,
                modifier = Modifier.fillMaxWidth(),
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
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 120.dp,
        ),
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
            MerchantKpiSection(kpis = state.data.kpis7d)
        }

        item {
            SectionBlock(
                eyebrow = "Actions rapides",
                title = "Gérer votre activité",
                subtitle = "Accédez vite aux actions marchandes les plus fréquentes.",
            ) {
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
        }

        item {
            TerminalSummarySection(summary = state.data.terminalsSummary)
        }

        item {
            TransactionsSection(
                title = "Transactions récentes",
                subtitle = "Lecture plus claire de votre activité la plus récente.",
                transactions = state.data.recentTransactions,
            )
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
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 120.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { DashboardHeader(role = role) }

        item {
            AgentPositionHighlight(balance = state.data.balance)
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
            SectionBlock(
                eyebrow = "Actions rapides",
                title = "Opérations agent",
                subtitle = "Démarrez vos opérations fréquentes sans détour.",
            ) {
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
        }

        if (state.data.alerts.isNotEmpty()) {
            item {
                AlertsSection(
                    title = "Alertes opérationnelles",
                    subtitle = "À surveiller avant vos prochains mouvements.",
                    alerts = state.data.alerts,
                )
            }
        }

        item {
            TransactionsSection(
                title = "Transactions récentes",
                subtitle = "Vos derniers encaissements, retraits et mouvements.",
                transactions = state.data.recentTransactions,
            )
        }

        item {
            RecentActivitiesSection(activities = state.data.recentActivities)
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
            text = when (role) {
                UserRole.CLIENT -> "Votre solde, vos cartes et vos derniers paiements en un coup d'œil."
                UserRole.MERCHANT -> "Une lecture claire de votre performance, de vos terminaux et de vos flux."
                UserRole.AGENT -> "Votre position cash, votre commission et vos opérations clés au même endroit."
                else -> "Un aperçu clair et rassurant de votre activité KORI."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionBlock(
    eyebrow: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = KoriPrimary.copy(alpha = 0.72f),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = KoriPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content()
        }
    }
}

@Composable
private fun RoleHintCard(
    title: String,
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(KoriAccent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = KoriPrimary,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AlertsSection(
    title: String,
    subtitle: String,
    alerts: List<AlertItem>,
) {
    SectionBlock(
        eyebrow = "Alertes",
        title = title,
        subtitle = subtitle,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            alerts.forEachIndexed { index, item ->
                AlertMessageCard(
                    title = when {
                        item.code.contains("SUSPEND", ignoreCase = true) -> "Action requise"
                        item.code.contains("LIMIT", ignoreCase = true) -> "Seuil à surveiller"
                        item.code.contains("FAIL", ignoreCase = true) -> "Incident récent"
                        else -> "Information utile"
                    },
                    message = item.message,
                    severe = index == 0,
                )
            }
        }
    }
}

@Composable
private fun AlertMessageCard(
    title: String,
    message: String,
    severe: Boolean,
) {
    val icon = if (severe) Icons.Outlined.WarningAmber else Icons.Outlined.Info

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (severe) KoriAccent.copy(alpha = 0.18f) else KoriSurfaceVariant,
        ),
        border = if (severe) {
            androidx.compose.foundation.BorderStroke(1.dp, KoriAccent.copy(alpha = 0.45f))
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (severe) KoriAccent.copy(alpha = 0.26f) else KoriSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = KoriPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = KoriPrimary,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CardsPreviewSection(
    cards: List<CardItem>,
    onOpenCards: () -> Unit,
) {
    SectionBlock(
        eyebrow = "Cartes",
        title = "Aperçu de vos cartes",
        subtitle = "Les cartes les plus récentes et leur état actuel.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            cards.forEachIndexed { index, card ->
                ClientCardPreviewItem(
                    modifier = Modifier,
                    card = card,
                    index = index + 1,
                )
            }

            OutlinedButton(
                onClick = onOpenCards,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("Voir toutes mes cartes")
            }
        }
    }
}

@Composable
private fun ClientCardPreviewItem(
    modifier: Modifier,
    card: CardItem,
    index: Int,
) {
    ClientCardListItem(card = card, index = index, modifier = modifier)
}

@Composable
private fun MerchantKpiSection(
    kpis: Kpis7dResponse,
) {
    SectionBlock(
        eyebrow = "Performance 7 jours",
        title = "KPIs marchands",
        subtitle = "Volume, nombre d'opérations et incidents récents.",
    ) {
        KPIWidget(
            title = "Synthèse glissante",
            txCount = kpis.txCount,
            txVolume = kpis.txVolume,
            failedCount = kpis.failedCount,
            chartPoints = listOf(18f, 24f, 20f, 28f, 34f, 30f, 38f),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HighlightStatCard(
                title = "Taux de réussite",
                value = if (kpis.txCount > 0) {
                    "${(((kpis.txCount - kpis.failedCount).toFloat() / kpis.txCount.toFloat()) * 100f).toInt()}%"
                } else {
                    "—"
                },
                icon = Icons.Outlined.PointOfSale,
                modifier = Modifier.weight(1f),
            )

            HighlightStatCard(
                title = "Transactions / jour",
                value = if (kpis.txCount > 0) {
                    "${(kpis.txCount / 7.0).toInt()}"
                } else {
                    "0"
                },
                icon = Icons.Outlined.History,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TerminalSummarySection(
    summary: TerminalsSummaryResponse,
) {
    SectionBlock(
        eyebrow = "Terminaux",
        title = "État du parc",
        subtitle = "Lecture rapide du volume total et des statuts dominants.",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HighlightStatCard(
                title = "Total",
                value = summary.total.toString(),
                icon = Icons.Outlined.Storefront,
                modifier = Modifier.weight(1f),
            )
            HighlightStatCard(
                title = "Hors ligne",
                value = summary.staleTerminals.toString(),
                icon = Icons.Outlined.ErrorOutline,
                modifier = Modifier.weight(1f),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            summary.byStatus.toList().sortedBy { it.first }.forEach { (status, count) ->
                MetricRow(
                    label = status,
                    value = count.toString(),
                )
            }
        }
    }
}

@Composable
private fun AgentPositionHighlight(
    balance: ActorBalanceResponse,
) {
    val cash = balance.balances
        .firstOrNull { it.kind == BalanceKind.CASH }
        ?.amount
        ?: 0L

    val commission = balance.balances
        .firstOrNull { it.kind == BalanceKind.COMMISSION }
        ?.amount
        ?: 0L

    val total = cash + commission

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(KoriAccent.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        tint = KoriPrimary,
                    )
                }

                Column {
                    Text(
                        text = "Position agent",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KoriPrimary,
                    )
                    Text(
                        text = "Cash disponible + commission cumulée",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Total mobilisable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatKmf(total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BalanceDetailCard(
                    label = "Cash",
                    amount = formatKmf(cash),
                    modifier = Modifier.weight(1f),
                )
                BalanceDetailCard(
                    label = "Commission",
                    amount = formatKmf(commission),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BalanceDetailCard(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TransactionsSection(
    title: String,
    subtitle: String,
    transactions: List<TransactionItemResponse>,
) {
    SectionBlock(
        eyebrow = "Transactions",
        title = title,
        subtitle = subtitle,
    ) {
        if (transactions.isEmpty()) {
            Text(
                text = "Aucune transaction récente à afficher.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val featured = transactions.first()
            FeaturedTransactionCard(transaction = featured)

            if (transactions.size > 1) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    transactions.drop(1).forEach { transaction ->
                        TransactionRowCard(transaction = transaction)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedTransactionCard(
    transaction: TransactionItemResponse,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = KoriAccent.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "Dernière opération",
                        style = MaterialTheme.typography.labelMedium,
                        color = KoriPrimary.copy(alpha = 0.72f),
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = formatKmf(transaction.amount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TypeChip(type = transaction.type)
                    StatusBadge(status = transaction.status)
                }
            }

            Text(
                text = transaction.counterparty.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = transaction.counterparty.phone
                    ?: transaction.counterparty.code
                    ?: transaction.transactionRef,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatIsoToDisplay(transaction.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = transaction.transactionRef,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RecentActivitiesSection(
    activities: List<ActivityItem>,
) {
    SectionBlock(
        eyebrow = "Activités",
        title = "Activités récentes",
        subtitle = "Chronologie récente de vos actions et événements utiles.",
    ) {
        if (activities.isEmpty()) {
            Text(
                text = "Aucune activité récente à afficher.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                activities.forEachIndexed { index, activity ->
                    AgentActivityCard(
                        activity = activity,
                        first = index == 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun AgentActivityCard(
    activity: ActivityItem,
    first: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (first) KoriSurfaceVariant else KoriSurface,
        ),
        border = if (first) {
            androidx.compose.foundation.BorderStroke(1.dp, KoriAccent.copy(alpha = 0.35f))
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (first) KoriAccent else KoriPrimary.copy(alpha = 0.40f)),
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = activity.action,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = KoriPrimary,
                )
                Text(
                    text = "${activity.resourceType} • ${activity.resourceRef}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatIsoToDisplay(activity.occurredAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HighlightStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(KoriAccent.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = KoriPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(KoriSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DashboardPill(
    text: String,
    highlighted: Boolean,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (highlighted) KoriAccent.copy(alpha = 0.18f) else KoriSurface,
            )
            .border(
                width = 1.dp,
                color = if (highlighted) {
                    KoriAccent.copy(alpha = 0.35f)
                } else {
                    KoriPrimary.copy(alpha = 0.10f)
                },
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = KoriPrimary,
            fontWeight = FontWeight.Medium,
        )
    }
}
