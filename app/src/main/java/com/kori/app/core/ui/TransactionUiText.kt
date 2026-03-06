package com.kori.app.core.ui

import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.core.model.transaction.TransactionType

fun TransactionType.displayLabel(): String = when (this) {
    TransactionType.CARD_PAYMENT -> "Paiement carte"
    TransactionType.CASH_IN -> "Cash-in"
    TransactionType.CLIENT_TRANSFER -> "Transfert client"
    TransactionType.MERCHANT_TRANSFER -> "Transfert marchand"
    TransactionType.MERCHANT_WITHDRAW -> "Retrait marchand"
    TransactionType.REVERSAL -> "Annulation"
    TransactionType.AGENT_BANK_DEPOSIT -> "Dépôt banque agent"
}

fun TransactionStatus.displayLabel(): String = when (this) {
    TransactionStatus.PENDING -> "En cours"
    TransactionStatus.COMPLETED -> "Succès"
    TransactionStatus.FAILED -> "Échec"
    TransactionStatus.REVERSED -> "Annulée"
}

fun TransactionStatus.timelineLabel(): String = when (this) {
    TransactionStatus.PENDING -> "Traitement en cours"
    TransactionStatus.COMPLETED -> "Transaction confirmée"
    TransactionStatus.FAILED -> "Transaction échouée"
    TransactionStatus.REVERSED -> "Transaction annulée"
}
