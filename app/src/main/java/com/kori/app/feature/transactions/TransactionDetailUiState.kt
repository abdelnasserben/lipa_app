package com.kori.app.feature.transactions

import com.kori.app.core.model.transaction.TransactionItemResponse

sealed interface TransactionDetailUiState {
    data object Loading : TransactionDetailUiState
    data class Error(val message: String) : TransactionDetailUiState
    data class Content(
        val transaction: TransactionItemResponse,
        val timeline: List<TransactionTimelineStep>,
    ) : TransactionDetailUiState
}

data class TransactionTimelineStep(
    val title: String,
    val isCompleted: Boolean,
)