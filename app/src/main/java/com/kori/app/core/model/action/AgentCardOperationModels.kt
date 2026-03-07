package com.kori.app.core.model.action

import com.kori.app.core.ui.FinancialInputRules

data class AgentCardEnrollDraft(
    val phoneNumber: String = FinancialInputRules.formatComorosPhoneForDisplay(""),
    val displayName: String = "",
    val cardUid: String = "",
    val pin: String = "",
)

data class AgentCardAddDraft(
    val phoneNumber: String = FinancialInputRules.formatComorosPhoneForDisplay(""),
    val cardUid: String = "",
    val pin: String = "",
)

enum class AgentCardTargetStatus {
    BLOCKED,
    LOST,
}

data class AgentCardStatusUpdateDraft(
    val cardUid: String = "",
    val targetStatus: AgentCardTargetStatus? = null,
    val reason: String = "",
)

data class AgentCardEnrollReceipt(
    val transactionId: String,
    val clientCode: String,
    val clientPhoneNumber: String,
    val cardUid: String,
    val cardPrice: Long,
    val agentCommission: Long,
    val clientCreated: Boolean,
    val clientAccountProfileCreated: Boolean,
)

data class AgentCardAddReceipt(
    val transactionId: String,
    val clientId: String,
    val cardUid: String,
    val cardPrice: Long,
    val agentCommission: Long,
)

data class AgentCardStatusUpdateReceipt(
    val subjectRef: String,
    val previousStatus: String,
    val newStatus: String,
)

sealed interface AgentCardEnrollResult {
    data class Success(
        val receipt: AgentCardEnrollReceipt,
    ) : AgentCardEnrollResult

    data class Failure(
        val code: FinancialErrorCode,
        val message: String,
    ) : AgentCardEnrollResult
}

sealed interface AgentCardAddResult {
    data class Success(
        val receipt: AgentCardAddReceipt,
    ) : AgentCardAddResult

    data class Failure(
        val code: FinancialErrorCode,
        val message: String,
    ) : AgentCardAddResult
}

sealed interface AgentCardStatusUpdateResult {
    data class Success(
        val receipt: AgentCardStatusUpdateReceipt,
    ) : AgentCardStatusUpdateResult

    data class Failure(
        val code: FinancialErrorCode,
        val message: String,
    ) : AgentCardStatusUpdateResult
}
