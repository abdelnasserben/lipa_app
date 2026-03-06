package com.kori.app.data.repository

import com.kori.app.core.model.activity.ActivityCategory
import com.kori.app.core.model.activity.ActivityFeedItem
import com.kori.app.core.model.activity.ActivityStatus
import com.kori.app.core.model.activity.ActivityType

data class ActivityQuery(
    val from: String? = null,
    val to: String? = null,
    val type: ActivityType? = null,
    val status: ActivityStatus? = null,
    val category: ActivityCategory? = null,
)

interface ActivityRepository {
    suspend fun getClientActivities(query: ActivityQuery): List<ActivityFeedItem>
    suspend fun getMerchantActivities(query: ActivityQuery): List<ActivityFeedItem>
    suspend fun getAgentActivities(query: ActivityQuery): List<ActivityFeedItem>
}
