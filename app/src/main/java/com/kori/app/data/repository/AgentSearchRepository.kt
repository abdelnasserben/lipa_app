package com.kori.app.data.repository

import com.kori.app.core.model.search.AgentSearchItem

interface AgentSearchRepository {
    suspend fun search(
        query: String,
        limit: Int = DEFAULT_LIMIT,
    ): List<AgentSearchItem>

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}
