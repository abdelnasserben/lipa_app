package com.kori.app.core.model.action

enum class ActionIntentType {
    CLIENT_TRANSFER,
    MERCHANT_TRANSFER,
    CASH_IN,
    MERCHANT_WITHDRAW,
}

data class ActionIntent(
    val type: ActionIntentType,
    val actor: String,
    val amount: Long,
)
