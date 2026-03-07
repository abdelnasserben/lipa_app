package com.kori.app.data.repository

import com.kori.app.core.model.dashboard.CardItem

/** API: GET /api/v1/client/me/cards */
interface ClientCardRepository {
    suspend fun getMyCards(): List<CardItem>
}
