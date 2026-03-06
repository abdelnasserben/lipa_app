package com.kori.app.data.mock

import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.core.model.action.MerchantTransferQuote
import com.kori.app.core.model.action.MerchantTransferReceipt
import com.kori.app.core.model.action.MerchantTransferResult
import com.kori.app.data.repository.MerchantTransferRepository
import kotlinx.coroutines.delay
import java.time.Instant
import java.util.UUID

class MockMerchantTransferRepository : MerchantTransferRepository {

    override suspend fun quoteTransfer(
        recipientMerchantCode: String,
        amount: Long,
        idempotencyKey: String,
    ): MerchantTransferQuote {
        delay(500)

        val fee = calculateFee(amount)
        return MerchantTransferQuote(
            recipientMerchantCode = recipientMerchantCode,
            amount = amount,
            fee = fee,
            totalDebited = amount + fee,
            idempotencyKey = idempotencyKey,
        )
    }

    override suspend fun submitTransfer(
        quote: MerchantTransferQuote,
    ): MerchantTransferResult {
        delay(900)

        return when {
            quote.recipientMerchantCode.endsWith("000") -> {
                MerchantTransferResult.Failure(
                    code = FinancialErrorCode.UNAUTHORIZED,
                    message = "Votre session n’autorise pas ce transfert marchand pour le moment.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.amount > 600_000L -> {
                MerchantTransferResult.Failure(
                    code = FinancialErrorCode.INSUFFICIENT_FUNDS,
                    message = "Le solde marchand disponible est insuffisant pour cette opération.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.amount in 300_000L..400_000L -> {
                MerchantTransferResult.Failure(
                    code = FinancialErrorCode.DAILY_LIMIT_EXCEEDED,
                    message = "Le plafond journalier marchand a été atteint.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.recipientMerchantCode.endsWith("999") -> {
                MerchantTransferResult.Failure(
                    code = FinancialErrorCode.INVALID_STATUS,
                    message = "Le marchand bénéficiaire ne peut pas recevoir ce transfert actuellement.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            else -> {
                MerchantTransferResult.Success(
                    receipt = MerchantTransferReceipt(
                        transactionRef = "TX-MER-${UUID.randomUUID().toString().take(8).uppercase()}",
                        recipientMerchantCode = quote.recipientMerchantCode,
                        amount = quote.amount,
                        fee = quote.fee,
                        totalDebited = quote.totalDebited,
                        createdAt = Instant.now().toString(),
                    ),
                )
            }
        }
    }

    private fun calculateFee(amount: Long): Long {
        return when {
            amount <= 50_000L -> 250L
            amount <= 150_000L -> 500L
            amount <= 300_000L -> 1_000L
            else -> 1_500L
        }
    }
}