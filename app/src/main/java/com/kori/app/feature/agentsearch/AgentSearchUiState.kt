package com.kori.app.feature.agentsearch

import com.kori.app.core.model.search.AgentSearchItem

sealed interface AgentSearchUiState {
    data object Idle : AgentSearchUiState
    data object Loading : AgentSearchUiState
    data class Content(val items: List<AgentSearchItem>) : AgentSearchUiState
    data class Empty(val query: String) : AgentSearchUiState
    data class Error(val message: String) : AgentSearchUiState
}
