package com.kori.app.data.repository.impl

import com.kori.app.core.model.action.ClientTransferQuote
import com.kori.app.core.model.action.ClientTransferResult
import com.kori.app.data.datasource.ClientTransferDataSource
import com.kori.app.data.repository.ClientTransferRepository

class ClientTransferRepositoryImpl(
    private val dataSource: ClientTransferDataSource,
) : ClientTransferRepository {
    override suspend fun quoteTransfer(
        recipientPhoneNumber: String,
        amount: Long,
        idempotencyKey: String,
    ): ClientTransferQuote = dataSource.quoteTransfer(recipientPhoneNumber, amount, idempotencyKey)

    override suspend fun submitTransfer(quote: ClientTransferQuote): ClientTransferResult = dataSource.submitTransfer(quote)
}
