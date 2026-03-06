package com.kori.app.feature.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.rememberDatePickerState
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.component.EmptyState
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.designsystem.component.TransactionRowCard
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.core.model.transaction.TransactionType
import com.kori.app.core.ui.epochMillisToLocalDateUtc
import com.kori.app.core.ui.formatIsoDateForInput
import com.kori.app.core.ui.isoToEpochMillisUtcStartOfDay
import com.kori.app.core.ui.localDateToUtcEndOfDayIso
import com.kori.app.core.ui.localDateToUtcStartOfDayIso

@Composable
fun TransactionsScreen(
    role: UserRole,
    uiState: TransactionsUiState,
    onRetry: () -> Unit,
    onApplyFilters: (TransactionsFilterState) -> Unit,
    onClearFilters: () -> Unit,
    onLoadMore: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        TransactionsUiState.Loading -> TransactionsLoading(modifier = modifier)

        is TransactionsUiState.Empty -> TransactionsEmpty(
            role = role,
            filters = uiState.filters,
            onClearFilters = onClearFilters,
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
            onApplyFilters = onApplyFilters,
            onClearFilters = onClearFilters,
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
    androidx.compose.foundation.layout.Column(
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
    filters: TransactionsFilterState,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header(role = role)

        EmptyState(
            title = if (filters.hasActiveFilters) {
                "Aucun résultat pour ces filtres"
            } else {
                "Aucune transaction"
            },
            message = if (filters.hasActiveFilters) {
                "Essayez d’élargir la période, le montant ou le type d’opération."
            } else {
                "Les opérations récentes apparaîtront ici dès qu’elles seront disponibles."
            },
            actionLabel = if (filters.hasActiveFilters) "Effacer les filtres" else null,
            onActionClick = if (filters.hasActiveFilters) onClearFilters else null,
        )
    }
}

@Composable
private fun TransactionsError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
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
    onApplyFilters: (TransactionsFilterState) -> Unit,
    onClearFilters: () -> Unit,
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
                onApplyFilters = onApplyFilters,
                onClearFilters = onClearFilters,
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
    androidx.compose.foundation.layout.Column(
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
    onApplyFilters: (TransactionsFilterState) -> Unit,
    onClearFilters: () -> Unit,
) {
    var selectedType by rememberSaveable(filters.selectedType) {
        mutableStateOf(filters.selectedType)
    }
    var selectedStatus by rememberSaveable(filters.selectedStatus) {
        mutableStateOf(filters.selectedStatus)
    }
    var fromIso by rememberSaveable(filters.from) {
        mutableStateOf(filters.from)
    }
    var toIso by rememberSaveable(filters.to) {
        mutableStateOf(filters.to)
    }
    var minAmount by rememberSaveable(filters.minAmount) {
        mutableStateOf(filters.minAmount)
    }
    var maxAmount by rememberSaveable(filters.maxAmount) {
        mutableStateOf(filters.maxAmount)
    }
    var selectedSort by rememberSaveable(filters.sort.name) {
        mutableStateOf(filters.sort)
    }

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(filters) {
        selectedType = filters.selectedType
        selectedStatus = filters.selectedStatus
        fromIso = filters.from
        toIso = filters.to
        minAmount = filters.minAmount
        maxAmount = filters.maxAmount
        selectedSort = filters.sort
    }

    androidx.compose.foundation.layout.Column(
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
            selectedItem = selectedType ?: "Tous",
            onSelected = { selected ->
                selectedType = selected.takeUnless { it == "Tous" }
            },
        )

        Text(
            text = "Statut",
            style = MaterialTheme.typography.labelLarge,
        )

        FilterChipRow(
            items = listOf("Tous") + TransactionStatus.entries.map { it.name },
            selectedItem = selectedStatus ?: "Tous",
            onSelected = { selected ->
                selectedStatus = selected.takeUnless { it == "Tous" }
            },
        )

        Text(
            text = "Période",
            style = MaterialTheme.typography.labelLarge,
        )

        ReadOnlyDateField(
            label = "Du",
            value = formatIsoDateForInput(fromIso),
            placeholder = "Sélectionner une date",
            onClick = { showFromDatePicker = true },
        )

        ReadOnlyDateField(
            label = "Au",
            value = formatIsoDateForInput(toIso),
            placeholder = "Sélectionner une date",
            onClick = { showToDatePicker = true },
        )

        Text(
            text = "Montant",
            style = MaterialTheme.typography.labelLarge,
        )

        OutlinedTextField(
            value = minAmount,
            onValueChange = { minAmount = it.filter { ch -> ch.isDigit() } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Minimum") },
            placeholder = { Text("Ex. 5000") },
        )

        OutlinedTextField(
            value = maxAmount,
            onValueChange = { maxAmount = it.filter { ch -> ch.isDigit() } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Maximum") },
            placeholder = { Text("Ex. 100000") },
        )

        Text(
            text = "Tri",
            style = MaterialTheme.typography.labelLarge,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(TransactionSortOption.entries) { sort ->
                FilterChip(
                    selected = sort == selectedSort,
                    onClick = { selectedSort = sort },
                    label = { Text(sort.label) },
                    colors = FilterChipDefaults.filterChipColors(),
                )
            }
        }

        Button(
            onClick = {
                onApplyFilters(
                    TransactionsFilterState(
                        selectedType = selectedType,
                        selectedStatus = selectedStatus,
                        from = fromIso,
                        to = toIso,
                        minAmount = minAmount,
                        maxAmount = maxAmount,
                        sort = selectedSort,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = KoriAccent,
                contentColor = KoriPrimary,
            ),
        ) {
            Text("Appliquer les filtres")
        }

        if (filters.hasActiveFilters) {
            TextButtonLike(
                text = "Effacer les filtres",
                onClick = {
                    selectedType = null
                    selectedStatus = null
                    fromIso = ""
                    toIso = ""
                    minAmount = ""
                    maxAmount = ""
                    selectedSort = TransactionSortOption.DATE_DESC
                    onClearFilters()
                },
            )
        }
    }

    if (showFromDatePicker) {
        FilterDatePickerDialog(
            initialSelectedDateMillis = isoToEpochMillisUtcStartOfDay(fromIso),
            onDismiss = { showFromDatePicker = false },
            onConfirm = { millis ->
                showFromDatePicker = false
                if (millis != null) {
                    fromIso = localDateToUtcStartOfDayIso(
                        epochMillisToLocalDateUtc(millis),
                    )
                }
            },
        )
    }

    if (showToDatePicker) {
        FilterDatePickerDialog(
            initialSelectedDateMillis = isoToEpochMillisUtcStartOfDay(toIso),
            onDismiss = { showToDatePicker = false },
            onConfirm = { millis ->
                showToDatePicker = false
                if (millis != null) {
                    toIso = localDateToUtcEndOfDayIso(
                        epochMillisToLocalDateUtc(millis),
                    )
                }
            },
        )
    }
}

@Composable
private fun ReadOnlyDateField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        singleLine = true,
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun FilterDatePickerDialog(
    initialSelectedDateMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit,
) {
    var selectedMillis by remember(initialSelectedDateMillis) {
        mutableLongStateOf(initialSelectedDateMillis ?: 0L)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButtonLike(
                text = "Valider",
                onClick = {
                    selectedMillis = datePickerState.selectedDateMillis ?: 0L
                    onConfirm(datePickerState.selectedDateMillis)
                },
            )
        },
        dismissButton = {
            TextButtonLike(
                text = "Annuler",
                onClick = onDismiss,
            )
        },
    ) {
        DatePicker(
            state = datePickerState,
        )
    }
}

@Composable
private fun TextButtonLike(
    text: String,
    onClick: () -> Unit,
) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text(text)
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