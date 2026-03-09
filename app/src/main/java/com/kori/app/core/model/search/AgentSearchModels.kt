package com.kori.app.core.model.search

data class AgentSearchItem(
    val entityType: String,
    val entityRef: String,
    val display: String,
    val status: String? = null,
)
