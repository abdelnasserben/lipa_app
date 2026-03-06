package com.kori.app.data.repository

import com.kori.app.core.model.action.MerchantTransferQuote
import com.kori.app.core.model.action.MerchantTransferResult

interface MerchantTransferRepository {
    suspend fun quoteTransfer(
        recipientMerchantCode: String,
        amount: Long,
        idempotencyKey: String,
    ): MerchantTransferQuote

    suspend fun submitTransfer(
        quote: MerchantTransferQuote,
    ): MerchantTransferResult
}