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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.R
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurface
import com.kori.app.core.designsystem.component.DetailRow
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.designsystem.component.SectionCard
import com.kori.app.core.designsystem.component.StatusBadge
import com.kori.app.core.designsystem.component.TimelineStepRow
import com.kori.app.core.designsystem.component.TypeChip
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.core.ui.displayLabel
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
    val showShareMessage = remember { mutableStateOf(false) }

    LaunchedEffect(showShareMessage.value) {
        if (showShareMessage.value) {
            kotlinx.coroutines.delay(1800)
            showShareMessage.value = false
        }
    }

    when (uiState) {
        TransactionDetailUiState.Loading -> {
            Column(
                modifier = modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.transaction_detail_loading),
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
                    title = stringResource(R.string.transaction_detail_error_title),
                    message = uiState.message,
                    onRetry = onRetry,
                )
            }
        }

        is TransactionDetailUiState.Content -> {
            TransactionDetailContent(
                transaction = uiState.transaction,
                timeline = uiState.timeline,
                onShareReceipt = {
                    showShareMessage.value = true
                    onShareReceipt()
                },
                showShareMessage = showShareMessage.value,
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
    showShareMessage: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 92.dp,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (showShareMessage) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = KoriSurface),
                ) {
                    Text(
                        text = stringResource(R.string.transaction_detail_share_message),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

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
                Text(stringResource(R.string.transaction_detail_share_action))
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
                text = stringResource(R.string.transaction_detail_timeline),
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
    SectionCard(title = stringResource(R.string.common_details)) {
        DetailRow(label = stringResource(R.string.common_reference), value = transaction.transactionRef)
        DetailRow(label = stringResource(R.string.common_amount), value = formatKmf(transaction.amount))
        DetailRow(label = stringResource(R.string.common_fees), value = formatKmf(transaction.fee ?: 0L))
        DetailRow(label = stringResource(R.string.transaction_detail_total_charged), value = formatKmf(transaction.totalDebited ?: transaction.amount))
        DetailRow(label = stringResource(R.string.common_status), value = transaction.status.displayLabel())
        DetailRow(label = stringResource(R.string.common_type), value = transaction.type.displayLabel())
        DetailRow(label = stringResource(R.string.transaction_detail_counterparty), value = transaction.counterparty.displayName)
        DetailRow(label = stringResource(R.string.profile_phone), value = transaction.counterparty.phone ?: "—")
        DetailRow(label = stringResource(R.string.profile_code), value = transaction.counterparty.code ?: "—")
        DetailRow(label = stringResource(R.string.common_date), value = formatIsoToDisplay(transaction.createdAt), showDivider = false)
    }
}
