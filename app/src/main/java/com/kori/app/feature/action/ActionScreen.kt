package com.kori.app.feature.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kori.app.core.model.UserRole

@Composable
fun ActionScreen(
    role: UserRole,
    onOpenClientTransfer: () -> Unit,
    onOpenMerchantTransfer: () -> Unit,
    onOpenAgentCashIn: () -> Unit,
    onOpenAgentMerchantWithdraw: () -> Unit,
    onOpenAgentCardEnroll: () -> Unit,
    onOpenAgentCardAdd: () -> Unit,
    onOpenAgentCardStatusUpdate: () -> Unit,
    onOpenAgentSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (role) {
        UserRole.CLIENT -> {
            ClientActionHome(
                onOpenClientTransfer = onOpenClientTransfer,
                modifier = modifier,
            )
        }

        UserRole.MERCHANT -> {
            MerchantActionHome(
                onOpenMerchantTransfer = onOpenMerchantTransfer,
                modifier = modifier,
            )
        }

        UserRole.AGENT -> {
            AgentActionHome(
                onOpenCashIn = onOpenAgentCashIn,
                onOpenMerchantWithdraw = onOpenAgentMerchantWithdraw,
                onOpenCardEnroll = onOpenAgentCardEnroll,
                onOpenCardAdd = onOpenAgentCardAdd,
                onOpenCardStatusUpdate = onOpenAgentCardStatusUpdate,
                onOpenAgentSearch = onOpenAgentSearch,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ClientActionHome(
    onOpenClientTransfer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Envoyer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = KoriPrimary,
            )
        }

        item {
            ActionEntryCard(
                title = "Transfert P2P",
                message = "Envoyez de l’argent à un proche en quelques étapes simples.",
                cta = "Commencer",
                onClick = onOpenClientTransfer,
            )
        }
    }
}

@Composable
private fun MerchantActionHome(
    onOpenMerchantTransfer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Transférer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = KoriPrimary,
            )
        }

        item {
            ActionEntryCard(
                title = "Transfert marchand",
                message = "Transférez des fonds vers un autre marchand de manière simple et sécurisée.",
                cta = "Commencer",
                onClick = onOpenMerchantTransfer,
            )
        }
    }
}

@Composable
private fun AgentActionHome(
    onOpenCashIn: () -> Unit,
    onOpenMerchantWithdraw: () -> Unit,
    onOpenCardEnroll: () -> Unit,
    onOpenCardAdd: () -> Unit,
    onOpenCardStatusUpdate: () -> Unit,
    onOpenAgentSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Opérations",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = KoriPrimary,
            )
        }

        item {
            ActionEntryCard(
                title = "Cash-in client",
                message = "Créditez le portefeuille d’un client à partir de son numéro de téléphone.",
                cta = "Lancer",
                onClick = onOpenCashIn,
            )
        }

        item {
            ActionEntryCard(
                title = "Retrait marchand",
                message = "Effectuez un retrait marchand et visualisez immédiatement la commission agent.",
                cta = "Lancer",
                onClick = onOpenMerchantWithdraw,
            )
        }

        item {
            ActionEntryCard(
                title = "Enrôlement de carte",
                message = "Associez une carte à un client en conformité avec le contrat API.",
                cta = "Lancer",
                onClick = onOpenCardEnroll,
            )
        }

        item {
            ActionEntryCard(
                title = "Ajout carte client",
                message = "Ajoutez une nouvelle carte à un client déjà existant.",
                cta = "Lancer",
                onClick = onOpenCardAdd,
            )
        }

        item {
            ActionEntryCard(
                title = "Modification statut carte",
                message = "Passez une carte existante en statut BLOCKED ou LOST.",
                cta = "Lancer",
                onClick = onOpenCardStatusUpdate,
            )
        }

        item {
            ActionEntryCard(
                title = "Recherche agent",
                message = "Recherchez rapidement un client, une carte ou un terminal.",
                cta = "Rechercher",
                onClick = onOpenAgentSearch,
            )
        }
    }
}

@Composable
private fun ActionEntryCard(
    title: String,
    message: String,
    cta: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KoriAccent,
                    contentColor = KoriPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Text(cta)
            }
        }
    }
}
