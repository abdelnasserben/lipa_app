package com.kori.app.data.mock

import com.kori.app.core.model.activity.ActivityFeedItem
import com.kori.app.data.repository.ActivityQuery
import com.kori.app.data.repository.ActivityRepository
import kotlinx.coroutines.delay

class MockActivityRepository : ActivityRepository {

    override suspend fun getClientActivities(query: ActivityQuery): List<ActivityFeedItem> {
        delay(250)
        return applyQuery(MockDataFactory.clientActivities(), query)
    }

    override suspend fun getMerchantActivities(query: ActivityQuery): List<ActivityFeedItem> {
        delay(250)
        return applyQuery(MockDataFactory.merchantActivities(), query)
    }

    override suspend fun getAgentActivities(query: ActivityQuery): List<ActivityFeedItem> {
        delay(250)
        return applyQuery(MockDataFactory.agentActivities(), query)
    }

    private fun applyQuery(
        source: List<ActivityFeedItem>,
        query: ActivityQuery,
    ): List<ActivityFeedItem> {
        return source
            .filter { query.type == null || it.type == query.type }
            .filter { query.status == null || it.status == query.status }
            .filter { query.category == null || it.category == query.category }
            .filter { query.from == null || it.occurredAt >= query.from }
            .filter { query.to == null || it.occurredAt <= query.to }
            .sortedByDescending { it.occurredAt }
    }
}
