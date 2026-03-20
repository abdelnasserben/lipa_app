package com.kori.app.feature.activity

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.R
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.activity.ActivityCategory
import com.kori.app.core.model.activity.ActivityFeedItem
import com.kori.app.core.model.activity.ActivityStatus
import com.kori.app.core.model.activity.ActivityType
import com.kori.app.data.repository.ActivityQuery
import com.kori.app.data.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ActivityViewModel(
    private val role: UserRole,
    private val repository: ActivityRepository,
    private val resources: Resources,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivityUiState>(ActivityUiState.Loading)
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    private var currentFilters = ActivityFilterState()

    init {
        refresh()
    }

    fun refresh() {
        loadWithFilters(currentFilters)
    }

    fun applyFilters(filters: ActivityFilterState) {
        currentFilters = filters
        loadWithFilters(filters)
    }

    fun clearFilters() {
        currentFilters = ActivityFilterState()
        loadWithFilters(currentFilters)
    }

    private fun loadWithFilters(filters: ActivityFilterState) {
        viewModelScope.launch {
            _uiState.value = ActivityUiState.Loading

            runCatching {
                when (role) {
                    UserRole.CLIENT -> repository.getClientActivities(buildQuery(filters))
                    UserRole.MERCHANT -> repository.getMerchantActivities(buildQuery(filters))
                    UserRole.AGENT -> repository.getAgentActivities(buildQuery(filters))
                }
            }.onSuccess { items ->
                _uiState.value = if (items.isEmpty()) {
                    ActivityUiState.Empty(filters = filters)
                } else {
                    ActivityUiState.Content(
                        ActivityContentState(
                            items = items,
                            sections = buildSections(items),
                            filters = filters,
                        ),
                    )
                }
            }.onFailure {
                _uiState.value = ActivityUiState.Error(
                    message = resources.getString(R.string.activity_error_message),
                )
            }
        }
    }

    private fun buildQuery(filters: ActivityFilterState): ActivityQuery {
        return ActivityQuery(
            from = filters.from.takeIf { it.isNotBlank() },
            to = filters.to.takeIf { it.isNotBlank() },
            type = filters.selectedType?.let(ActivityType::valueOf),
            status = filters.selectedStatus?.let(ActivityStatus::valueOf),
            category = filters.selectedCategory?.let(ActivityCategory::valueOf),
        )
    }

    private fun buildSections(items: List<ActivityFeedItem>): List<ActivitySection> {
        val today = LocalDate.now()
        val locale = resources.configuration.locales[0] ?: Locale.getDefault()
        val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", locale)

        return items
            .groupBy { item ->
                runCatching {
                    Instant.parse(item.occurredAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }.getOrNull()
            }
            .toSortedMap(compareByDescending<LocalDate?> { it })
            .map { (date, groupedItems) ->
                val title = when (date) {
                    null -> resources.getString(R.string.activity_section_other)
                    today -> resources.getString(R.string.activity_section_today)
                    today.minusDays(1) -> resources.getString(R.string.activity_section_yesterday)
                    else -> formatter.format(date)
                        .replaceFirstChar { char ->
                            if (char.isLowerCase()) {
                                char.titlecase(locale)
                            } else {
                                char.toString()
                            }
                        }
                }

                ActivitySection(
                    title = title,
                    items = groupedItems.sortedByDescending { it.occurredAt },
                )
            }
    }

    companion object {
        fun factory(
            role: UserRole,
            repository: ActivityRepository,
            resources: Resources,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ActivityViewModel(
                        role = role,
                        repository = repository,
                        resources = resources,
                    ) as T
                }
            }
        }
    }
}
