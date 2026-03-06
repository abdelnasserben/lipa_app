package com.kori.app.feature.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.component.CollapsibleFiltersCard
import com.kori.app.core.designsystem.component.CountPill
import com.kori.app.core.designsystem.component.EmptyState
import com.kori.app.core.designsystem.component.ErrorState
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.activity.ActivityCategory
import com.kori.app.core.model.activity.ActivityFeedItem
import com.kori.app.core.model.activity.ActivityStatus
import com.kori.app.core.model.activity.ActivityType
import com.kori.app.core.ui.displayLabel
import com.kori.app.core.ui.formatIsoToDisplay
import com.kori.app.core.ui.formatKmf

@Composable
fun ActivityScreen(
    role: UserRole,
    uiState: ActivityUiState,
    onRetry: () -> Unit,
    onApplyFilters: (ActivityFilterState) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        ActivityUiState.Loading -> ActivityLoading(modifier)
        is ActivityUiState.Error -> ActivityError(uiState.message, onRetry, modifier)
        is ActivityUiState.Empty -> ActivityEmpty(role, uiState.filters, onClearFilters, modifier)
        is ActivityUiState.Content -> ActivityContent(
            role = role,
            state = uiState.state,
            onApplyFilters = onApplyFilters,
            onClearFilters = onClearFilters,
            modifier = modifier,
        )
    }
}

@Composable
private fun ActivityLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ActivityTopHeader(role = null, count = null)
        CircularProgressIndicator()
        Text(
            text = "Chargement de l’historique…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActivityError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ActivityTopHeader(role = null, count = null)
        ErrorState(
            title = "Chargement indisponible",
            message = message,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun ActivityEmpty(
    role: UserRole,
    filters: ActivityFilterState,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ActivityTopHeader(role = role, count = 0)
        EmptyState(
            title = if (filters.hasActiveFilters) "Aucun résultat" else "Aucune activité",
            message = if (filters.hasActiveFilters) {
                "Essayez d’élargir les filtres appliqués."
            } else {
                "Vos événements récents apparaîtront ici."
            },
            actionLabel = if (filters.hasActiveFilters) "Effacer les filtres" else null,
            onActionClick = if (filters.hasActiveFilters) onClearFilters else null,
        )
    }
}

@Composable
private fun ActivityContent(
    role: UserRole,
    state: ActivityContentState,
    onApplyFilters: (ActivityFilterState) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ActivityTopHeader(
                role = role,
                count = state.items.size,
            )
        }

        item {
            ActivityFiltersBar(
                role = role,
                filters = state.filters,
                onApplyFilters = onApplyFilters,
                onClearFilters = onClearFilters,
            )
        }

        state.sections.forEach { section ->
            item(key = "section-${section.title}") {
                ActivitySectionHeader(
                    title = section.title,
                    count = section.items.size,
                )
            }

            items(
                items = section.items,
                key = { it.eventRef },
            ) { item ->
                ActivityTimelineItem(item = item)
            }
        }
    }
}

@Composable
private fun ActivityTopHeader(
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
                text = "Activité",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = when (role) {
                    UserRole.CLIENT -> "Retrouvez vos paiements, transferts et événements liés à votre carte."
                    UserRole.MERCHANT -> "Suivez vos encaissements, transferts et la santé de vos terminaux."
                    UserRole.AGENT -> "Gardez une vue claire sur les cash-in, retraits marchands et opérations terrain."
                    null -> "Consultez vos événements récents en un seul endroit."
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
                        text = "triés du plus récent au plus ancien",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityFiltersBar(
    role: UserRole,
    filters: ActivityFilterState,
    onApplyFilters: (ActivityFilterState) -> Unit,
    onClearFilters: () -> Unit,
) {
    val typeOptions = roleTypeOptions(role)

    var selectedType by rememberSaveable(filters.selectedType) {
        mutableStateOf(filters.selectedType)
    }
    var selectedStatus by rememberSaveable(filters.selectedStatus) {
        mutableStateOf(filters.selectedStatus)
    }
    var selectedCategory by rememberSaveable(filters.selectedCategory) {
        mutableStateOf(filters.selectedCategory)
    }
    var from by rememberSaveable(filters.from) {
        mutableStateOf(filters.from)
    }
    var to by rememberSaveable(filters.to) {
        mutableStateOf(filters.to)
    }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(filters) {
        selectedType = filters.selectedType
        selectedStatus = filters.selectedStatus
        selectedCategory = filters.selectedCategory
        from = filters.from
        to = filters.to
    }

    val localFilters = ActivityFilterState(
        selectedType = selectedType,
        selectedStatus = selectedStatus,
        selectedCategory = selectedCategory,
        from = from,
        to = to,
    )

    val activeFiltersCount = remember(
        selectedType,
        selectedStatus,
        selectedCategory,
        from,
        to,
    ) {
        countActivityActiveFilters(
            selectedType = selectedType,
            selectedStatus = selectedStatus,
            selectedCategory = selectedCategory,
            from = from,
            to = to,
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
                selectedCategory = null
                from = ""
                to = ""
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
                AssistChip(
                    onClick = {
                        val nextFilters = if (from.isBlank() && to.isBlank()) {
                            ActivityFilterState(
                                selectedType = selectedType,
                                selectedStatus = selectedStatus,
                                selectedCategory = selectedCategory,
                                from = "ACTIVE",
                                to = "ACTIVE",
                            )
                        } else {
                            ActivityFilterState(
                                selectedType = selectedType,
                                selectedStatus = selectedStatus,
                                selectedCategory = selectedCategory,
                                from = "",
                                to = "",
                            )
                        }

                        from = nextFilters.from
                        to = nextFilters.to
                        onApplyFilters(nextFilters)
                    },
                    label = {
                        Text(
                            if (from.isBlank() && to.isBlank()) {
                                "Toutes périodes"
                            } else {
                                "Période active"
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

            items(typeOptions) { type ->
                val isSelected = selectedType == type
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedType = type.takeUnless { isSelected }
                        onApplyFilters(
                            ActivityFilterState(
                                selectedType = selectedType,
                                selectedStatus = selectedStatus,
                                selectedCategory = selectedCategory,
                                from = from,
                                to = to,
                            ),
                        )
                    },
                    label = { Text(ActivityType.valueOf(type).displayLabel()) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    ),
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ActivityStatus.entries) { status ->
                val isSelected = selectedStatus == status.name
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedStatus = status.name.takeUnless { isSelected }
                        onApplyFilters(
                            ActivityFilterState(
                                selectedType = selectedType,
                                selectedStatus = selectedStatus,
                                selectedCategory = selectedCategory,
                                from = from,
                                to = to,
                            ),
                        )
                    },
                    label = { Text(status.displayLabel()) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        selectedContainerColor = statusColor(status).copy(alpha = 0.14f),
                    ),
                )
            }
        }

        if (role != UserRole.CLIENT) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ActivityCategory.entries) { category ->
                    val isSelected = selectedCategory == category.name
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = category.name.takeUnless { isSelected }
                            onApplyFilters(
                                ActivityFilterState(
                                    selectedType = selectedType,
                                    selectedStatus = selectedStatus,
                                    selectedCategory = selectedCategory,
                                    from = from,
                                    to = to,
                                ),
                            )
                        },
                        label = { Text(category.displayLabel()) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                        ),
                    )
                }
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
                )
                TextButton(
                    onClick = {
                        selectedType = null
                        selectedStatus = null
                        selectedCategory = null
                        from = ""
                        to = ""
                        onClearFilters()
                    },
                ) {
                    Text("Réinitialiser")
                }
            }
        }
    }
}

@Composable
private fun ActivitySectionHeader(
    title: String,
    count: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = if (count <= 1) "$count activité" else "$count activités",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActivityTimelineItem(item: ActivityFeedItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        TimelineColumn(item = item)
        ActivityCard(
            item = item,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TimelineColumn(item: ActivityFeedItem) {
    val visuals = activityVisuals(item)

    Column(
        modifier = Modifier.padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(visuals.iconTint),
        )
        Spacer(
            modifier = Modifier
                .padding(top = 4.dp)
                .width(2.dp)
                .height(88.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun ActivityCard(
    item: ActivityFeedItem,
    modifier: Modifier = Modifier,
) {
    val visuals = activityVisuals(item)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = visuals.containerColor,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(visuals.iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = visuals.icon,
                    contentDescription = null,
                    tint = visuals.iconTint,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (item.amount != null) {
                        Text(
                            text = formatKmf(item.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                }

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusBadge(status = item.status)

                    TypePill(
                        text = item.type.displayLabel(),
                        tint = visuals.iconTint,
                    )
                }

                Text(
                    text = "${formatIsoToDisplay(item.occurredAt)} • ${item.eventRef}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ActivityStatus?) {
    val color = when (status) {
        ActivityStatus.COMPLETED -> Color(0xFF1F8F63)
        ActivityStatus.PENDING -> Color(0xFFD98F00)
        ActivityStatus.FAILED -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.outline
    }

    val label = status?.displayLabel() ?: "Info"

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun TypePill(
    text: String,
    tint: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tint.copy(alpha = 0.10f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class ActivityVisuals(
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val containerColor: Color,
)

@Composable
private fun activityVisuals(item: ActivityFeedItem): ActivityVisuals {
    val scheme = MaterialTheme.colorScheme

    return when (item.type) {
        ActivityType.PAYMENT,
        ActivityType.COLLECTION -> ActivityVisuals(
            icon = Icons.Outlined.Payments,
            iconTint = scheme.primary,
            iconBackground = scheme.primary.copy(alpha = 0.14f),
            containerColor = scheme.primary.copy(alpha = 0.05f),
        )

        ActivityType.TRANSFER,
        ActivityType.MERCHANT_WITHDRAW -> ActivityVisuals(
            icon = Icons.Outlined.SwapHoriz,
            iconTint = scheme.secondary,
            iconBackground = scheme.secondary.copy(alpha = 0.14f),
            containerColor = scheme.secondary.copy(alpha = 0.06f),
        )

        ActivityType.CARD -> ActivityVisuals(
            icon = Icons.Outlined.CreditCard,
            iconTint = Color(0xFF6B4EFF),
            iconBackground = Color(0xFF6B4EFF).copy(alpha = 0.12f),
            containerColor = Color(0xFF6B4EFF).copy(alpha = 0.05f),
        )

        ActivityType.TERMINAL -> ActivityVisuals(
            icon = Icons.Outlined.PointOfSale,
            iconTint = Color(0xFF1B6EF3),
            iconBackground = Color(0xFF1B6EF3).copy(alpha = 0.12f),
            containerColor = Color(0xFF1B6EF3).copy(alpha = 0.05f),
        )

        ActivityType.CASH_IN,
        ActivityType.FIELD_OPERATION -> ActivityVisuals(
            icon = Icons.Outlined.AccountBalanceWallet,
            iconTint = Color(0xFF14936B),
            iconBackground = Color(0xFF14936B).copy(alpha = 0.12f),
            containerColor = Color(0xFF14936B).copy(alpha = 0.05f),
        )
    }
}

private fun buildDescriptionLine(item: ActivityFeedItem): String {
    return buildString {
        append(item.description)
        append(" • ")
        append(item.status?.displayLabel() ?: item.category.displayLabel())
    }
}

@Composable
private fun statusColor(status: ActivityStatus): Color {
    return when (status) {
        ActivityStatus.PENDING -> Color(0xFFD98F00)
        ActivityStatus.COMPLETED -> Color(0xFF1F8F63)
        ActivityStatus.FAILED -> MaterialTheme.colorScheme.error
    }
}

private fun activeFiltersSummary(filters: ActivityFilterState): String {
    val parts = buildList {
        filters.selectedType?.let { add(ActivityType.valueOf(it).displayLabel()) }
        filters.selectedStatus?.let { add(ActivityStatus.valueOf(it).displayLabel()) }
        filters.selectedCategory?.let { add(ActivityCategory.valueOf(it).displayLabel()) }
        if (filters.from.isNotBlank() || filters.to.isNotBlank()) {
            add("Période")
        }
    }
    return parts.joinToString(" • ")
}

private fun countActivityActiveFilters(
    selectedType: String?,
    selectedStatus: String?,
    selectedCategory: String?,
    from: String,
    to: String,
): Int {
    var count = 0
    if (selectedType != null) count++
    if (selectedStatus != null) count++
    if (selectedCategory != null) count++
    if (from.isNotBlank() || to.isNotBlank()) count++
    return count
}

private fun roleTypeOptions(role: UserRole): List<String> = when (role) {
    UserRole.CLIENT -> listOf(
        ActivityType.PAYMENT.name,
        ActivityType.TRANSFER.name,
        ActivityType.CARD.name,
    )

    UserRole.MERCHANT -> listOf(
        ActivityType.COLLECTION.name,
        ActivityType.TRANSFER.name,
        ActivityType.TERMINAL.name,
    )

    UserRole.AGENT -> listOf(
        ActivityType.CASH_IN.name,
        ActivityType.MERCHANT_WITHDRAW.name,
        ActivityType.FIELD_OPERATION.name,
    )
}