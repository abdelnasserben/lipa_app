package com.kori.app.core.model.activity

import com.kori.app.core.model.common.CurrencyCode

enum class ActivityType {
    PAYMENT,
    TRANSFER,
    CARD,
    COLLECTION,
    TERMINAL,
    CASH_IN,
    MERCHANT_WITHDRAW,
    FIELD_OPERATION,
}

enum class ActivityStatus {
    PENDING,
    COMPLETED,
    FAILED,
}

enum class ActivityCategory {
    PAYMENT,
    TRANSFER,
    CARD,
    TERMINAL,
    TERRAIN,
}

data class ActivityFeedItem(
    val eventRef: String,
    val occurredAt: String,
    val title: String,
    val description: String,
    val type: ActivityType,
    val status: ActivityStatus? = null,
    val category: ActivityCategory,
    val amount: Long? = null,
    val currency: CurrencyCode = CurrencyCode.KMF,
)
