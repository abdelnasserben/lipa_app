package com.kori.app.feature.cards

import com.kori.app.core.model.dashboard.CardItem

sealed interface ClientCardsUiState {
    data object Loading : ClientCardsUiState
    data object Empty : ClientCardsUiState
    data class Error(val message: String) : ClientCardsUiState
    data class Content(val cards: List<CardItem>) : ClientCardsUiState
}
