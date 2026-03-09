package com.kori.app.data.mock

import com.kori.app.core.model.search.AgentSearchItem
import com.kori.app.data.repository.AgentSearchRepository
import kotlinx.coroutines.delay

class MockAgentSearchRepository : AgentSearchRepository {

    private val source = listOf(
        AgentSearchItem("CLIENT", "CLI-0301", "Amina Soilihi · +269 355 10 20", "ACTIVE"),
        AgentSearchItem("CLIENT", "CLI-0448", "Ibrahim Ali · +269 399 80 10", "INACTIVE"),
        AgentSearchItem("CARD", "CARD-001", "Carte client principale", "ACTIVE"),
        AgentSearchItem("CARD", "CARD-002", "Carte secours client", "BLOCKED"),
        AgentSearchItem("TERMINAL", "TERM-001", "Terminal Mbeni Express", "ONLINE"),
        AgentSearchItem("TERMINAL", "TERM-004", "Terminal Mutsamudu", "OFFLINE"),
        AgentSearchItem("MERCHANT", "MER-0061", "Boutique Rahma", "ACTIVE"),
    )

    override suspend fun search(query: String, limit: Int): List<AgentSearchItem> {
        delay(300)
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return emptyList()

        return source
            .filter { item ->
                item.entityType.lowercase().contains(normalized) ||
                    item.entityRef.lowercase().contains(normalized) ||
                    item.display.lowercase().contains(normalized)
            }
            .take(limit)
    }
}
