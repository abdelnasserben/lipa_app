package com.kori.app.feature.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.data.repository.ClientCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientCardsViewModel(
    private val repository: ClientCardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClientCardsUiState>(ClientCardsUiState.Loading)
    val uiState: StateFlow<ClientCardsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ClientCardsUiState.Loading

            runCatching {
                repository.getMyCards()
            }.onSuccess { cards ->
                _uiState.value = if (cards.isEmpty()) {
                    ClientCardsUiState.Empty
                } else {
                    ClientCardsUiState.Content(cards.sortedByDescending { it.createdAt })
                }
            }.onFailure {
                _uiState.value = ClientCardsUiState.Error(
                    message = "Impossible de charger vos cartes pour le moment.",
                )
            }
        }
    }

    companion object {
        fun factory(repository: ClientCardRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(ClientCardsViewModel::class.java))
                    return ClientCardsViewModel(repository) as T
                }
            }
        }
    }
}
