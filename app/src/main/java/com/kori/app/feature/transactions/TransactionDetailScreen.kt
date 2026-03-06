package com.kori.app.feature.transactions

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurface
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.designsystem.component.StatusBadge
import com.kori.app.core.designsystem.component.TimelineStepRow
import com.kori.app.core.designsystem.component.TypeChip
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.core.ui.formatIsoToDisplay
import com.kori.app.core.ui.formatKmf

@Composable
fun TransactionDetailScreen(
    uiState: TransactionDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onShareReceipt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val _onBack = onBack

    when (uiState) {
        TransactionDetailUiState.Loading -> {
            Column(
                modifier = modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Chargement du détail de la transaction…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        is TransactionDetailUiState.Error -> {
            Column(
                modifier = modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ErrorState(
                    title = "Détail indisponible",
                    message = uiState.message,
                    onRetry = onRetry,
                )
            }
        }

        is TransactionDetailUiState.Content -> {
            TransactionDetailContent(
                transaction = uiState.transaction,
                timeline = uiState.timeline,
                onShareReceipt = onShareReceipt,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun TransactionDetailContent(
    transaction: TransactionItemResponse,
    timeline: List<TransactionTimelineStep>,
    onShareReceipt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SummaryCard(transaction = transaction)
        }

        item {
            TimelineCard(timeline = timeline)
        }

        item {
            DetailsCard(transaction = transaction)
        }

        item {
            Button(
                onClick = onShareReceipt,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KoriAccent,
                    contentColor = KoriPrimary,
                ),
            ) {
                Text("Partager le reçu")
            }
        }
    }
}

@Composable
private fun SummaryCard(
    transaction: TransactionItemResponse,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TypeChip(type = transaction.type)
            StatusBadge(status = transaction.status)

            Text(
                text = formatKmf(transaction.amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = transaction.counterparty.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = formatIsoToDisplay(transaction.createdAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimelineCard(
    timeline: List<TransactionTimelineStep>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            timeline.forEachIndexed { index, step ->
                TimelineStepRow(
                    title = step.title,
                    isCompleted = step.isCompleted,
                    isLast = index == timeline.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun DetailsCard(
    transaction: TransactionItemResponse,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Détails",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            DetailLine("Référence", transaction.transactionRef)
            DetailLine("Montant", formatKmf(transaction.amount))
            DetailLine("Frais", formatKmf(transaction.fee ?: 0L))
            DetailLine("Total débité", formatKmf(transaction.totalDebited ?: transaction.amount))
            DetailLine("Statut", transaction.status.name)
            DetailLine("Type", transaction.type.name)
            DetailLine("Contrepartie", transaction.counterparty.displayName)
            DetailLine("Téléphone", transaction.counterparty.phone ?: "—")
            DetailLine("Code", transaction.counterparty.code ?: "—")
            DetailLine("Date", formatIsoToDisplay(transaction.createdAt))
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}