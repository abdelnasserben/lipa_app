package com.kori.app.core.model.balance

import com.kori.app.core.model.common.BalanceKind
import com.kori.app.core.model.common.CurrencyCode

data class BalanceItemResponse(
    val kind: BalanceKind,
    val amount: Long,
)

data class ActorBalanceResponse(
    val ownerRef: String,
    val currency: CurrencyCode,
    val balances: List<BalanceItemResponse>,
)