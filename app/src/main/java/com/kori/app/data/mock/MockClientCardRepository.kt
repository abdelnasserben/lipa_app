package com.kori.app.data.mock

import com.kori.app.core.model.dashboard.CardItem
import com.kori.app.data.repository.ClientCardRepository
import kotlinx.coroutines.delay

class MockClientCardRepository : ClientCardRepository {
    override suspend fun getMyCards(): List<CardItem> {
        delay(200)
        return MockDataFactory.clientDashboard().cards
    }
}
