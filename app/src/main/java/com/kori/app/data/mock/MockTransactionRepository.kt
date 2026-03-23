package com.kori.app.data.mock

import com.kori.app.core.model.common.CursorPagedResponse
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.data.repository.TransactionQuery
import com.kori.app.data.repository.TransactionRepository
import kotlinx.coroutines.delay

class MockTransactionRepository : TransactionRepository {

    override suspend fun getClientTransactions(
        query: TransactionQuery,
    ): CursorPagedResponse<TransactionItemResponse> {
        delay(250)
        val filtered = applyQuery(
            source = MockDataFactory.clientTransactions(),
            query = query,
        )
        return MockDataFactory.pagedTransactions(
            source = filtered,
            cursor = query.cursor,
            limit = query.limit,
        )
    }

    override suspend fun getMerchantTransactions(
        query: TransactionQuery,
    ): CursorPagedResponse<TransactionItemResponse> {
        delay(250)
        val filtered = applyQuery(
            source = MockDataFactory.merchantTransactions(),
            query = query,
        )
        return MockDataFactory.pagedTransactions(
            source = filtered,
            cursor = query.cursor,
            limit = query.limit,
        )
    }

    override suspend fun getAgentTransactions(
        query: TransactionQuery,
    ): CursorPagedResponse<TransactionItemResponse> {
        delay(250)
        val filtered = applyQuery(
            source = MockDataFactory.agentTransactions(),
            query = query,
        )
        return MockDataFactory.pagedTransactions(
            source = filtered,
            cursor = query.cursor,
            limit = query.limit,
        )
    }

    override suspend fun getClientTransactionDetail(transactionRef: String): TransactionItemResponse {
        delay(150)
        return MockDataFactory.clientTransactions().first { it.transactionRef == transactionRef }
    }

    override suspend fun getMerchantTransactionDetail(transactionRef: String): TransactionItemResponse {
        delay(150)
        return MockDataFactory.merchantTransactions().first { it.transactionRef == transactionRef }
    }

    override suspend fun getAgentTransactionDetail(transactionRef: String): TransactionItemResponse {
        delay(150)
        return MockDataFactory.agentTransactions().first { it.transactionRef == transactionRef }
    }

    private fun applyQuery(
        source: List<TransactionItemResponse>,
        query: TransactionQuery,
    ): List<TransactionItemResponse> {
        val filtered = source
            .filter { item ->
                query.type == null || item.type == query.type
            }
            .filter { item ->
                query.status == null || item.status == query.status
            }
            .filter { item ->
                query.minAmount == null || item.amount >= query.minAmount
            }
            .filter { item ->
                query.maxAmount == null || item.amount <= query.maxAmount
            }
            .filter { item ->
                query.from == null || item.createdAt >= query.from
            }
            .filter { item ->
                query.to == null || item.createdAt <= query.to
            }

        return when (query.sort) {
            "createdAt" -> filtered.sortedBy { it.createdAt }
            "-createdAt" -> filtered.sortedByDescending { it.createdAt }
            else -> filtered.sortedByDescending { it.createdAt }
        }
    }
}
