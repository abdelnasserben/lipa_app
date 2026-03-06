package com.kori.app.data.mock

import com.kori.app.core.model.action.ClientTransferQuote
import com.kori.app.core.model.action.ClientTransferReceipt
import com.kori.app.core.model.action.ClientTransferResult
import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.data.repository.ClientTransferRepository
import kotlinx.coroutines.delay
import java.time.Instant
import java.util.UUID

class MockClientTransferRepository : ClientTransferRepository {

    override suspend fun quoteTransfer(
        recipientPhoneNumber: String,
        amount: Long,
        idempotencyKey: String,
    ): ClientTransferQuote {
        delay(500)

        val fee = calculateFee(amount)
        return ClientTransferQuote(
            recipientPhoneNumber = recipientPhoneNumber,
            amount = amount,
            fee = fee,
            totalDebited = amount + fee,
            idempotencyKey = idempotencyKey,
        )
    }

    override suspend fun submitTransfer(
        quote: ClientTransferQuote,
    ): ClientTransferResult {
        delay(900)

        return when {
            quote.recipientPhoneNumber.endsWith("000") -> {
                ClientTransferResult.Failure(
                    code = FinancialErrorCode.UNAUTHORIZED,
                    message = "Votre session n’autorise pas cette opération pour le moment.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.amount > 200_000L -> {
                ClientTransferResult.Failure(
                    code = FinancialErrorCode.INSUFFICIENT_FUNDS,
                    message = "Le solde disponible est insuffisant pour ce transfert.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.amount in 95_000L..120_000L -> {
                ClientTransferResult.Failure(
                    code = FinancialErrorCode.DAILY_LIMIT_EXCEEDED,
                    message = "Votre plafond journalier a été atteint.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.recipientPhoneNumber.endsWith("999") -> {
                ClientTransferResult.Failure(
                    code = FinancialErrorCode.INVALID_STATUS,
                    message = "Le bénéficiaire ne peut pas recevoir ce transfert actuellement.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            else -> {
                ClientTransferResult.Success(
                    receipt = ClientTransferReceipt(
                        transactionRef = "TX-P2P-${UUID.randomUUID().toString().take(8).uppercase()}",
                        recipientPhoneNumber = quote.recipientPhoneNumber,
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
            amount <= 5_000L -> 50L
            amount <= 20_000L -> 150L
            amount <= 50_000L -> 300L
            else -> 500L
        }
    }
}