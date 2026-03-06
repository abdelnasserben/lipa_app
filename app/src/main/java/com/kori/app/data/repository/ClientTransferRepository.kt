package com.kori.app.data.repository

import com.kori.app.core.model.action.ClientTransferQuote
import com.kori.app.core.model.action.ClientTransferResult

interface ClientTransferRepository {
    suspend fun quoteTransfer(
        recipientPhoneNumber: String,
        amount: Long,
        idempotencyKey: String,
    ): ClientTransferQuote

    suspend fun submitTransfer(
        quote: ClientTransferQuote,
    ): ClientTransferResult
}