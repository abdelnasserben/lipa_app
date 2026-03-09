package com.kori.app.data.repository.impl

import com.kori.app.core.model.common.CursorPagedResponse
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.data.datasource.TransactionDataSource
import com.kori.app.data.repository.TransactionQuery
import com.kori.app.data.repository.TransactionRepository

class TransactionRepositoryImpl(
    private val dataSource: TransactionDataSource,
) : TransactionRepository {
    override suspend fun getClientTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse> =
        dataSource.getClientTransactions(query)

    override suspend fun getMerchantTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse> =
        dataSource.getMerchantTransactions(query)

    override suspend fun getAgentTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse> =
        dataSource.getAgentTransactions(query)
}
