package com.kori.app.domain.idempotency

import com.kori.app.core.model.action.ActionIntent
import com.kori.app.data.idempotency.PendingAction
import com.kori.app.data.idempotency.PendingActionStatus
import com.kori.app.data.idempotency.PendingActionStore
import java.util.UUID

class IdempotencyManager(
    private val pendingActionStore: PendingActionStore,
) {

    fun getOrCreateIdempotencyKey(intent: ActionIntent): String {
        val existing = pendingActionStore.getByIntent(intent)
        if (existing != null) return existing.idempotencyKey

        val idempotencyKey = UUID.randomUUID().toString()
        pendingActionStore.upsert(
            PendingAction(
                intent = intent,
                idempotencyKey = idempotencyKey,
                status = PendingActionStatus.READY,
            ),
        )
        return idempotencyKey
    }

    fun start(intent: ActionIntent, idempotencyKey: String): Boolean {
        val tracked = pendingActionStore.getByIdempotencyKey(idempotencyKey)
            ?: PendingAction(
                intent = intent,
                idempotencyKey = idempotencyKey,
                status = PendingActionStatus.READY,
            )

        if (tracked.status == PendingActionStatus.IN_PROGRESS) {
            return false
        }

        pendingActionStore.upsert(
            tracked.copy(
                status = PendingActionStatus.IN_PROGRESS,
            ),
        )
        return true
    }

    fun onSuccess(idempotencyKey: String) {
        pendingActionStore.remove(idempotencyKey)
    }

    fun onFailure(idempotencyKey: String) {
        val tracked = pendingActionStore.getByIdempotencyKey(idempotencyKey) ?: return
        pendingActionStore.upsert(tracked.copy(status = PendingActionStatus.FAILED))
    }

    fun clear(intent: ActionIntent) {
        val tracked = pendingActionStore.getByIntent(intent) ?: return
        pendingActionStore.remove(tracked.idempotencyKey)
    }
}
