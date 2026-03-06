package com.kori.app.feature.activity

import com.kori.app.core.model.activity.ActivityFeedItem

data class ActivityFilterState(
    val from: String = "",
    val to: String = "",
    val selectedType: String? = null,
    val selectedStatus: String? = null,
    val selectedCategory: String? = null,
) {
    val hasActiveFilters: Boolean
        get() = from.isNotBlank() ||
            to.isNotBlank() ||
            selectedType != null ||
            selectedStatus != null ||
            selectedCategory != null
}

data class ActivitySection(
    val title: String,
    val items: List<ActivityFeedItem>,
)

data class ActivityContentState(
    val items: List<ActivityFeedItem> = emptyList(),
    val sections: List<ActivitySection> = emptyList(),
    val filters: ActivityFilterState = ActivityFilterState(),
)

sealed interface ActivityUiState {
    data object Loading : ActivityUiState
    data class Empty(val filters: ActivityFilterState) : ActivityUiState
    data class Error(val message: String) : ActivityUiState
    data class Content(val state: ActivityContentState) : ActivityUiState
}
