package com.kori.app.core.ui

import android.content.res.Resources
import androidx.annotation.StringRes
import com.kori.app.R
import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.core.model.transaction.TransactionType

@StringRes
fun TransactionType.displayLabelResId(): Int = when (this) {
    TransactionType.CARD_PAYMENT -> R.string.transaction_type_card_payment
    TransactionType.CASH_IN -> R.string.transaction_type_cash_in
    TransactionType.CLIENT_TRANSFER -> R.string.transaction_type_client_transfer
    TransactionType.MERCHANT_TRANSFER -> R.string.transaction_type_merchant_transfer
    TransactionType.MERCHANT_WITHDRAW -> R.string.transaction_type_merchant_withdraw
    TransactionType.REVERSAL -> R.string.transaction_type_reversal
    TransactionType.AGENT_BANK_DEPOSIT -> R.string.transaction_type_agent_bank_deposit
}

fun TransactionType.displayLabel(resources: Resources): String = resources.getString(displayLabelResId())

@StringRes
fun TransactionStatus.displayLabelResId(): Int = when (this) {
    TransactionStatus.PENDING -> R.string.transaction_status_pending
    TransactionStatus.COMPLETED -> R.string.transaction_status_completed
    TransactionStatus.FAILED -> R.string.transaction_status_failed
    TransactionStatus.REVERSED -> R.string.transaction_status_reversed
}

fun TransactionStatus.displayLabel(resources: Resources): String = resources.getString(displayLabelResId())

@StringRes
fun TransactionStatus.timelineLabelResId(): Int = when (this) {
    TransactionStatus.PENDING -> R.string.transaction_timeline_pending
    TransactionStatus.COMPLETED -> R.string.transaction_timeline_completed
    TransactionStatus.FAILED -> R.string.transaction_timeline_failed
    TransactionStatus.REVERSED -> R.string.transaction_timeline_reversed
}

fun TransactionStatus.timelineLabel(resources: Resources): String = resources.getString(timelineLabelResId())
