package com.kori.app.core.model

enum class UserRole(
    val label: String,
    val dashboardTitle: String,
    val actionLabel: String,
    val historyLabel: String,
) {
    CLIENT(
        label = "Client",
        dashboardTitle = "Espace client",
        actionLabel = "Envoyer",
        historyLabel = "Historique",
    ),
    MERCHANT(
        label = "Marchand",
        dashboardTitle = "Espace marchand",
        actionLabel = "Transférer",
        historyLabel = "Activités",
    ),
    AGENT(
        label = "Agent",
        dashboardTitle = "Espace agent",
        actionLabel = "Opérations",
        historyLabel = "Activités",
    ),
}