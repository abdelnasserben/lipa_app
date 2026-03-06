package com.kori.app.core.model.profile

import com.kori.app.core.model.common.ActorStatus

data class ClientProfileResponse(
    val code: String,
    val displayName: String,
    val phone: String,
    val status: ActorStatus,
    val createdAt: String,
)

data class MerchantProfileResponse(
    val code: String,
    val displayName: String,
    val status: ActorStatus,
    val createdAt: String,
)

data class AgentProfileResponse(
    val code: String,
    val displayName: String,
    val status: ActorStatus,
    val createdAt: String,
)