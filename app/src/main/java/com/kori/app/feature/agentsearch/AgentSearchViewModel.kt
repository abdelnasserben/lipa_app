package com.kori.app.feature.agentsearch

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.R
import com.kori.app.data.repository.AgentSearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgentSearchViewModel(
    private val repository: AgentSearchRepository,
    private val resources: Resources,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgentSearchUiState>(AgentSearchUiState.Idle)
    val uiState: StateFlow<AgentSearchUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        val normalized = query.trim()
        if (normalized.isBlank()) {
            _uiState.value = AgentSearchUiState.Idle
            return
        }

        viewModelScope.launch {
            _uiState.value = AgentSearchUiState.Loading
            runCatching {
                repository.search(normalized)
            }.onSuccess { results ->
                _uiState.value = if (results.isEmpty()) {
                    AgentSearchUiState.Empty(query = normalized)
                } else {
                    AgentSearchUiState.Content(results)
                }
            }.onFailure {
                _uiState.value = AgentSearchUiState.Error(
                    message = resources.getString(R.string.agent_search_error_message),
                )
            }
        }
    }

    companion object {
        fun factory(
            repository: AgentSearchRepository,
            resources: Resources,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(AgentSearchViewModel::class.java))
                    return AgentSearchViewModel(repository, resources) as T
                }
            }
        }
    }
}
