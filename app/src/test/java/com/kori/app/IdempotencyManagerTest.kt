package com.kori.app

import com.kori.app.core.model.action.ActionIntent
import com.kori.app.core.model.action.ActionIntentType
import com.kori.app.data.idempotency.InMemoryPendingActionStore
import com.kori.app.domain.idempotency.IdempotencyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IdempotencyManagerTest {

    private val store = InMemoryPendingActionStore()
    private val manager = IdempotencyManager(store)

    @Test
    fun `getOrCreateIdempotencyKey returns same key for same intent`() {
        val intent = ActionIntent(
            type = ActionIntentType.CLIENT_TRANSFER,
            actor = "+2693000000",
            amount = 12_000L,
        )

        val first = manager.getOrCreateIdempotencyKey(intent)
        val second = manager.getOrCreateIdempotencyKey(intent)

        assertEquals(first, second)
    }

    @Test
    fun `start prevents duplicate in progress execution`() {
        val intent = ActionIntent(
            type = ActionIntentType.CASH_IN,
            actor = "+2693000000",
            amount = 5_000L,
        )
        val key = manager.getOrCreateIdempotencyKey(intent)

        assertTrue(manager.start(intent, key))
        assertFalse(manager.start(intent, key))

        manager.onFailure(key)

        assertTrue(manager.start(intent, key))
    }

    @Test
    fun `onSuccess clears pending action`() {
        val intent = ActionIntent(
            type = ActionIntentType.MERCHANT_WITHDRAW,
            actor = "MRC-12345",
            amount = 9_000L,
        )
        val key = manager.getOrCreateIdempotencyKey(intent)

        assertNotNull(store.getByIdempotencyKey(key))

        manager.onSuccess(key)

        assertEquals(null, store.getByIdempotencyKey(key))
    }
}
