package com.kori.app.core.ui

import com.kori.app.core.model.activity.ActivityCategory
import com.kori.app.core.model.activity.ActivityStatus
import com.kori.app.core.model.activity.ActivityType

fun ActivityType.displayLabel(): String = when (this) {
    ActivityType.PAYMENT -> "Paiement"
    ActivityType.TRANSFER -> "Transfert"
    ActivityType.CARD -> "Carte"
    ActivityType.COLLECTION -> "Encaissement"
    ActivityType.TERMINAL -> "Terminal"
    ActivityType.CASH_IN -> "Cash-in"
    ActivityType.MERCHANT_WITHDRAW -> "Retrait marchand"
    ActivityType.FIELD_OPERATION -> "Opération terrain"
}

fun ActivityStatus.displayLabel(): String = when (this) {
    ActivityStatus.PENDING -> "En cours"
    ActivityStatus.COMPLETED -> "Succès"
    ActivityStatus.FAILED -> "Échec"
}

fun ActivityCategory.displayLabel(): String = when (this) {
    ActivityCategory.PAYMENT -> "Paiement"
    ActivityCategory.TRANSFER -> "Transfert"
    ActivityCategory.CARD -> "Carte"
    ActivityCategory.TERMINAL -> "Terminal"
    ActivityCategory.TERRAIN -> "Terrain"
}
