package com.kori.app.feature.transactions

import android.app.DatePickerDialog
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.component.CollapsibleFiltersCard
import com.kori.app.core.designsystem.component.CountPill
import com.kori.app.core.designsystem.component.EmptyState
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.designsystem.component.TransactionRowCard
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.core.model.transaction.TransactionType
import com.kori.app.core.ui.displayLabel
import com.kori.app.core.ui.epochMillisToLocalDateUtc
import com.kori.app.core.ui.formatIsoDateForInput
import com.kori.app.core.ui.isoToEpochMillisUtcStartOfDay
import com.kori.app.core.ui.localDateToUtcEndOfDayIso
import com.kori.app.core.ui.localDateToUtcStartOfDayIso
import java.util.Calendar
import java.util.TimeZone

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
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TransactionsTopHeader(role = null, count = null)
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
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TransactionsTopHeader(role = role, count = 0)

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
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TransactionsTopHeader(role = null, count = null)
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
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 16.dp,
            bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TransactionsTopHeader(
                role = role,
                count = state.items.size,
            )
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
private fun TransactionsTopHeader(
    role: UserRole?,
    count: Int?,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    null -> "Consultez vos opérations récentes en un seul endroit."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (count != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CountPill(
                        text = if (count <= 1) "$count élément" else "$count éléments",
                    )
                    Text(
                        text = "triés selon vos filtres actifs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
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
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(filters) {
        selectedType = filters.selectedType
        selectedStatus = filters.selectedStatus
        fromIso = filters.from
        toIso = filters.to
        minAmount = filters.minAmount
        maxAmount = filters.maxAmount
        selectedSort = filters.sort
    }

    val localFilters = TransactionsFilterState(
        selectedType = selectedType,
        selectedStatus = selectedStatus,
        from = fromIso,
        to = toIso,
        minAmount = minAmount,
        maxAmount = maxAmount,
        sort = selectedSort,
    )

    val activeFiltersCount = remember(
        selectedType,
        selectedStatus,
        fromIso,
        toIso,
        minAmount,
        maxAmount,
        selectedSort,
    ) {
        countActiveFilters(
            selectedType = selectedType,
            selectedStatus = selectedStatus,
            fromIso = fromIso,
            toIso = toIso,
            minAmount = minAmount,
            maxAmount = maxAmount,
            sort = selectedSort,
        )
    }

    CollapsibleFiltersCard(
        title = "Filtres",
        activeCount = activeFiltersCount,
        expanded = isExpanded,
        onToggleExpanded = { isExpanded = !isExpanded },
        onClearFilters = if (activeFiltersCount > 0) {
            {
                selectedType = null
                selectedStatus = null
                fromIso = ""
                toIso = ""
                minAmount = ""
                maxAmount = ""
                selectedSort = TransactionSortOption.DATE_DESC
                onClearFilters()
            }
        } else {
            null
        },
    ) {
        Text(
            text = "Filtres rapides",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                val hasPeriod = fromIso.isNotBlank() || toIso.isNotBlank()
                AssistChip(
                    onClick = {
                        if (hasPeriod) {
                            fromIso = ""
                            toIso = ""
                        }
                    },
                    label = {
                        Text(
                            if (hasPeriod) {
                                "Période active"
                            } else {
                                "Toutes périodes"
                            },
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                        )
                    },
                )
            }

            item {
                val hasAmount = minAmount.isNotBlank() || maxAmount.isNotBlank()
                AssistChip(
                    onClick = {
                        if (hasAmount) {
                            minAmount = ""
                            maxAmount = ""
                        }
                    },
                    label = {
                        Text(
                            if (hasAmount) {
                                "Montant actif"
                            } else {
                                "Tous montants"
                            },
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.SwapHoriz,
                            contentDescription = null,
                        )
                    },
                )
            }
        }

        Text(
            text = "Type",
            style = MaterialTheme.typography.labelLarge,
        )

        val typeOptions = listOf(
            FilterOption(value = "Tous", label = "Tous"),
        ) + TransactionType.entries.map { type ->
            FilterOption(value = type.name, label = type.displayLabel())
        }

        FilterChipRow(
            items = typeOptions,
            selectedItem = selectedType ?: "Tous",
            onSelected = { selected ->
                selectedType = selected.takeUnless { it == "Tous" }
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
        )

        Text(
            text = "Statut",
            style = MaterialTheme.typography.labelLarge,
        )

        val statusOptions = listOf(
            FilterOption(value = "Tous", label = "Tous"),
        ) + TransactionStatus.entries.map { status ->
            FilterOption(value = status.name, label = status.displayLabel())
        }

        FilterChipRow(
            items = statusOptions,
            selectedItem = selectedStatus ?: "Tous",
            onSelected = { selected ->
                selectedStatus = selected.takeUnless { it == "Tous" }
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
        )

        Text(
            text = "Période",
            style = MaterialTheme.typography.labelLarge,
        )

        NativeDateField(
            label = "Du",
            value = formatIsoDateForInput(fromIso),
            placeholder = "Sélectionner une date",
            initialIso = fromIso,
            onDateSelected = { localDateIsoUtcStart ->
                fromIso = localDateIsoUtcStart
            },
            onClear = if (fromIso.isNotBlank()) {
                { fromIso = "" }
            } else {
                null
            },
            toUtcIso = { millis ->
                localDateToUtcStartOfDayIso(
                    epochMillisToLocalDateUtc(millis),
                )
            },
        )

        NativeDateField(
            label = "Au",
            value = formatIsoDateForInput(toIso),
            placeholder = "Sélectionner une date",
            initialIso = toIso,
            onDateSelected = { localDateIsoUtcEnd ->
                toIso = localDateIsoUtcEnd
            },
            onClear = if (toIso.isNotBlank()) {
                { toIso = "" }
            } else {
                null
            },
            toUtcIso = { millis ->
                localDateToUtcEndOfDayIso(
                    epochMillisToLocalDateUtc(millis),
                )
            },
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
                    onClick = {
                        selectedSort = sort
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
                    label = { Text(sort.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    ),
                )
            }
        }

        if (localFilters.hasActiveFilters) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = activeFiltersSummary(localFilters),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(12.dp))

                TextButton(
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
                ) {
                    Text("Réinitialiser")
                }
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
                isExpanded = false
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = KoriAccent,
                contentColor = KoriPrimary,
            ),
        ) {
            Text("Appliquer les filtres")
        }
    }
}

@Composable
private fun NativeDateField(
    label: String,
    value: String,
    placeholder: String,
    initialIso: String,
    onDateSelected: (String) -> Unit,
    onClear: (() -> Unit)?,
    toUtcIso: (Long) -> String,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AssistChip(
                onClick = {
                    val initialMillis = isoToEpochMillisUtcStartOfDay(initialIso)
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = initialMillis ?: System.currentTimeMillis()
                    }

                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onDateSelected(toUtcIso(selectedCalendar.timeInMillis))
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                    ).show()
                },
                label = { Text("Choisir une date") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                    )
                },
            )

            if (onClear != null) {
                TextButton(onClick = onClear) {
                    Text("Effacer")
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    items: List<FilterOption>,
    selectedItem: String,
    onSelected: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 4.dp),
    ) {
        items(items) { item ->
            FilterChip(
                selected = item.value == selectedItem,
                onClick = { onSelected(item.value) },
                label = { Text(item.label) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                ),
            )
        }
    }
}

private fun countActiveFilters(
    selectedType: String?,
    selectedStatus: String?,
    fromIso: String,
    toIso: String,
    minAmount: String,
    maxAmount: String,
    sort: TransactionSortOption,
): Int {
    var count = 0

    if (selectedType != null) count++
    if (selectedStatus != null) count++
    if (fromIso.isNotBlank()) count++
    if (toIso.isNotBlank()) count++
    if (minAmount.isNotBlank()) count++
    if (maxAmount.isNotBlank()) count++
    if (sort != TransactionSortOption.DATE_DESC) count++

    return count
}

private fun activeFiltersSummary(filters: TransactionsFilterState): String {
    val parts = buildList {
        filters.selectedType?.let { add(TransactionType.valueOf(it).displayLabel()) }
        filters.selectedStatus?.let { add(TransactionStatus.valueOf(it).displayLabel()) }

        if (filters.from.isNotBlank() || filters.to.isNotBlank()) {
            add("Période")
        }

        if (filters.minAmount.isNotBlank() || filters.maxAmount.isNotBlank()) {
            add("Montant")
        }

        if (filters.sort != TransactionSortOption.DATE_DESC) {
            add(filters.sort.label)
        }
    }

    return parts.joinToString(" • ")
}

private data class FilterOption(
    val value: String,
    val label: String,
)