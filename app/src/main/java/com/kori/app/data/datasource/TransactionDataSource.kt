package com.kori.app.data.datasource

import com.kori.app.core.model.common.CursorPagedResponse
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.data.repository.TransactionQuery

interface TransactionDataSource {
    suspend fun getClientTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse>
    suspend fun getMerchantTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse>
    suspend fun getAgentTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse>
    suspend fun getClientTransactionDetail(transactionRef: String): TransactionItemResponse
    suspend fun getMerchantTransactionDetail(transactionRef: String): TransactionItemResponse
    suspend fun getAgentTransactionDetail(transactionRef: String): TransactionItemResponse
}
