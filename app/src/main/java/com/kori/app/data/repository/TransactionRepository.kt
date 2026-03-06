package com.kori.app.data.repository

import com.kori.app.core.model.common.CursorPagedResponse
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.core.model.transaction.TransactionType

data class TransactionQuery(
    val type: TransactionType? = null,
    val status: TransactionStatus? = null,
    val from: String? = null,
    val to: String? = null,
    val minAmount: Long? = null,
    val maxAmount: Long? = null,
    val sort: String = "-createdAt",
    val cursor: String? = null,
    val limit: Int = 10,
)

interface TransactionRepository {
    suspend fun getClientTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse>
    suspend fun getMerchantTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse>
    suspend fun getAgentTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse>
}