package com.kori.app.data.idempotency

import com.kori.app.core.model.action.ActionIntent

enum class PendingActionStatus {
    READY,
    IN_PROGRESS,
    FAILED,
}

data class PendingAction(
    val intent: ActionIntent,
    val idempotencyKey: String,
    val status: PendingActionStatus,
)

interface PendingActionStore {
    fun getByIntent(intent: ActionIntent): PendingAction?
    fun getByIdempotencyKey(idempotencyKey: String): PendingAction?
    fun upsert(action: PendingAction)
    fun remove(idempotencyKey: String)
}

class InMemoryPendingActionStore : PendingActionStore {
    private val byKey = linkedMapOf<String, PendingAction>()
    private val keyByIntent = linkedMapOf<ActionIntent, String>()

    override fun getByIntent(intent: ActionIntent): PendingAction? {
        val key = keyByIntent[intent] ?: return null
        return byKey[key]
    }

    override fun getByIdempotencyKey(idempotencyKey: String): PendingAction? {
        return byKey[idempotencyKey]
    }

    override fun upsert(action: PendingAction) {
        byKey[action.idempotencyKey] = action
        keyByIntent[action.intent] = action.idempotencyKey
    }

    override fun remove(idempotencyKey: String) {
        val removed = byKey.remove(idempotencyKey) ?: return
        keyByIntent.remove(removed.intent)
    }
}
