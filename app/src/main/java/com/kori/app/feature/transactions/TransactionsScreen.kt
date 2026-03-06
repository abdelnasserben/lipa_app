package com.kori.app.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.component.EmptyState
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.designsystem.component.TransactionRowCard
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.core.model.transaction.TransactionType

@Composable
fun TransactionsScreen(
    role: UserRole,
    uiState: TransactionsUiState,
    onRetry: () -> Unit,
    onTypeSelected: (String?) -> Unit,
    onStatusSelected: (String?) -> Unit,
    onLoadMore: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        TransactionsUiState.Loading -> TransactionsLoading(modifier = modifier)

        TransactionsUiState.Empty -> TransactionsEmpty(
            role = role,
            modifier = modifier,
        )

        is TransactionsUiState.Error -> TransactionsError(
            message = uiState.message,
            onRetry = onRetry,
            modifier = modifier,
        )

        is TransactionsUiState.Content -> TransactionsContent(
            role = role,
            state = uiState.state,
            onTypeSelected = onTypeSelected,
            onStatusSelected = onStatusSelected,
            onLoadMore = onLoadMore,
            onTransactionClick = onTransactionClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun TransactionsLoading(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        CircularProgressIndicator()
        Text(
            text = "Chargement de vos opérations…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TransactionsEmpty(
    role: UserRole,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header(role = role)
        EmptyState(
            title = "Aucune transaction",
            message = "Les opérations récentes apparaîtront ici dès qu’elles seront disponibles.",
        )
    }
}

@Composable
private fun TransactionsError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Transactions",
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
private fun TransactionsContent(
    role: UserRole,
    state: TransactionsContentState,
    onTypeSelected: (String?) -> Unit,
    onStatusSelected: (String?) -> Unit,
    onLoadMore: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Header(role = role)
        }

        item {
            FiltersSection(
                filters = state.filters,
                onTypeSelected = onTypeSelected,
                onStatusSelected = onStatusSelected,
            )
        }

        items(
            items = state.items,
            key = { it.transactionRef },
        ) { transaction ->
            TransactionRowCard(
                transaction = transaction,
                onClick = {
                    onTransactionClick(transaction.transactionRef)
                },
            )
        }

        if (state.hasMore) {
            item {
                Button(
                    onClick = onLoadMore,
                    enabled = !state.isLoadingMore,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KoriAccent,
                        contentColor = KoriPrimary,
                    ),
                ) {
                    if (state.isLoadingMore) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Charger plus")
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    role: UserRole,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = when (role) {
                UserRole.CLIENT -> "Suivez simplement vos paiements, transferts et mouvements récents."
                UserRole.MERCHANT -> "Retrouvez les encaissements et opérations marchandes les plus récentes."
                UserRole.AGENT -> "Consultez rapidement les opérations terrain et dépôts récents."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FiltersSection(
    filters: TransactionsFilterState,
    onTypeSelected: (String?) -> Unit,
    onStatusSelected: (String?) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Filtres",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "Type",
            style = MaterialTheme.typography.labelLarge,
        )

        FilterChipRow(
            items = listOf("Tous") + TransactionType.entries.map { it.name },
            selectedItem = filters.selectedType ?: "Tous",
            onSelected = { selected ->
                onTypeSelected(selected.takeUnless { it == "Tous" })
            },
        )

        Text(
            text = "Statut",
            style = MaterialTheme.typography.labelLarge,
        )

        FilterChipRow(
            items = listOf("Tous") + TransactionStatus.entries.map { it.name },
            selectedItem = filters.selectedStatus ?: "Tous",
            onSelected = { selected ->
                onStatusSelected(selected.takeUnless { it == "Tous" })
            },
        )
    }
}

@Composable
private fun FilterChipRow(
    items: List<String>,
    selectedItem: String,
    onSelected: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 4.dp),
    ) {
        items(items) { item ->
            FilterChip(
                selected = item == selectedItem,
                onClick = { onSelected(item) },
                label = { Text(item) },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}