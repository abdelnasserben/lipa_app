package com.kori.app.data.mock

import com.kori.app.core.model.action.AgentCashInQuote
import com.kori.app.core.model.action.AgentCashInReceipt
import com.kori.app.core.model.action.AgentCashInResult
import com.kori.app.core.model.action.AgentMerchantWithdrawQuote
import com.kori.app.core.model.action.AgentMerchantWithdrawReceipt
import com.kori.app.core.model.action.AgentMerchantWithdrawResult
import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.data.repository.AgentActionRepository
import kotlinx.coroutines.delay
import java.time.Instant
import java.util.UUID

class MockAgentActionRepository : AgentActionRepository {

    override suspend fun quoteCashIn(
        phoneNumber: String,
        amount: Long,
        idempotencyKey: String,
    ): AgentCashInQuote {
        delay(450)
        return AgentCashInQuote(
            phoneNumber = phoneNumber,
            amount = amount,
            fee = 0L,
            idempotencyKey = idempotencyKey,
        )
    }

    override suspend fun submitCashIn(
        quote: AgentCashInQuote,
    ): AgentCashInResult {
        delay(850)

        return when {
            quote.phoneNumber.endsWith("000") -> {
                AgentCashInResult.Failure(
                    code = FinancialErrorCode.UNAUTHORIZED,
                    message = "Votre session n’autorise pas cette opération pour le moment.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.amount > 500_000L -> {
                AgentCashInResult.Failure(
                    code = FinancialErrorCode.DAILY_LIMIT_EXCEEDED,
                    message = "Le plafond autorisé pour ce cash-in a été atteint.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.phoneNumber.endsWith("999") -> {
                AgentCashInResult.Failure(
                    code = FinancialErrorCode.INVALID_STATUS,
                    message = "Le client ciblé ne peut pas être crédité actuellement.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            else -> {
                AgentCashInResult.Success(
                    receipt = AgentCashInReceipt(
                        transactionRef = "TX-CASH-${UUID.randomUUID().toString().take(8).uppercase()}",
                        clientPhoneNumber = quote.phoneNumber,
                        amount = quote.amount,
                        fee = quote.fee,
                        createdAt = Instant.now().toString(),
                    ),
                )
            }
        }
    }

    override suspend fun quoteMerchantWithdraw(
        merchantCode: String,
        amount: Long,
        idempotencyKey: String,
    ): AgentMerchantWithdrawQuote {
        delay(450)
        val fee = calculateWithdrawFee(amount)
        val commission = calculateCommission(amount)

        return AgentMerchantWithdrawQuote(
            merchantCode = merchantCode,
            amount = amount,
            fee = fee,
            commission = commission,
            totalDebitedMerchant = amount + fee,
            idempotencyKey = idempotencyKey,
        )
    }

    override suspend fun submitMerchantWithdraw(
        quote: AgentMerchantWithdrawQuote,
    ): AgentMerchantWithdrawResult {
        delay(900)

        return when {
            quote.merchantCode.endsWith("000") -> {
                AgentMerchantWithdrawResult.Failure(
                    code = FinancialErrorCode.UNAUTHORIZED,
                    message = "Votre session n’autorise pas ce retrait marchand pour le moment.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.amount > 700_000L -> {
                AgentMerchantWithdrawResult.Failure(
                    code = FinancialErrorCode.INSUFFICIENT_FUNDS,
                    message = "Le solde marchand est insuffisant pour cette opération.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.amount in 300_000L..450_000L -> {
                AgentMerchantWithdrawResult.Failure(
                    code = FinancialErrorCode.DAILY_LIMIT_EXCEEDED,
                    message = "Le plafond journalier du marchand a été atteint.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            quote.merchantCode.endsWith("999") -> {
                AgentMerchantWithdrawResult.Failure(
                    code = FinancialErrorCode.INVALID_STATUS,
                    message = "Le marchand bénéficiaire ne peut pas effectuer ce retrait actuellement.",
                    idempotencyKey = quote.idempotencyKey,
                )
            }

            else -> {
                AgentMerchantWithdrawResult.Success(
                    receipt = AgentMerchantWithdrawReceipt(
                        transactionRef = "TX-MWD-${UUID.randomUUID().toString().take(8).uppercase()}",
                        merchantCode = quote.merchantCode,
                        amount = quote.amount,
                        fee = quote.fee,
                        commission = quote.commission,
                        totalDebitedMerchant = quote.totalDebitedMerchant,
                        createdAt = Instant.now().toString(),
                    ),
                )
            }
        }
    }

    private fun calculateWithdrawFee(amount: Long): Long {
        return when {
            amount <= 50_000L -> 250L
            amount <= 150_000L -> 500L
            amount <= 300_000L -> 1_000L
            else -> 1_500L
        }
    }

    private fun calculateCommission(amount: Long): Long {
        return when {
            amount <= 50_000L -> 100L
            amount <= 150_000L -> 250L
            amount <= 300_000L -> 500L
            else -> 750L
        }
    }
}