package com.kori.app.data.repository.impl

import com.kori.app.core.model.action.MerchantTransferQuote
import com.kori.app.core.model.action.MerchantTransferResult
import com.kori.app.data.datasource.MerchantTransferDataSource
import com.kori.app.data.repository.MerchantTransferRepository

class MerchantTransferRepositoryImpl(
    private val dataSource: MerchantTransferDataSource,
) : MerchantTransferRepository {
    override suspend fun quoteTransfer(
        recipientMerchantCode: String,
        amount: Long,
        idempotencyKey: String,
    ): MerchantTransferQuote = dataSource.quoteTransfer(recipientMerchantCode, amount, idempotencyKey)

    override suspend fun submitTransfer(quote: MerchantTransferQuote): MerchantTransferResult = dataSource.submitTransfer(quote)
}
