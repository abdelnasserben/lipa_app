package com.kori.app.navigation

sealed class KoriDestination(
    val route: String,
    val label: String,
) {
    data object RolePicker : KoriDestination(
        route = "role_picker",
        label = "Accueil",
    )

    data object AuthWelcome : KoriDestination(
        route = "auth_welcome",
        label = "Connexion",
    )

    data object AuthBrowserMock : KoriDestination(
        route = "auth_browser_mock",
        label = "Keycloak",
    )

    data object AuthCallback : KoriDestination(
        route = "auth_callback",
        label = "Traitement",
    )

    data object AuthSuccess : KoriDestination(
        route = "auth_success",
        label = "Succès",
    )

    data object Dashboard : KoriDestination(
        route = "dashboard",
        label = "Dashboard",
    )

    data object Transactions : KoriDestination(
        route = "transactions",
        label = "Transactions",
    )

    data object TransactionDetail : KoriDestination(
        route = "transaction_detail/{transactionRef}",
        label = "Détail",
    ) {
        fun createRoute(transactionRef: String): String {
            return "transaction_detail/$transactionRef"
        }
    }

    data object ClientTransfer : KoriDestination(
        route = "client_transfer",
        label = "Transfert client",
    )

    data object MerchantTransfer : KoriDestination(
        route = "merchant_transfer",
        label = "Transfert marchand",
    )

    data object AgentCashIn : KoriDestination(
        route = "agent_cash_in",
        label = "Cash-in",
    )

    data object AgentMerchantWithdraw : KoriDestination(
        route = "agent_merchant_withdraw",
        label = "Retrait marchand",
    )

    data object AgentCardEnroll : KoriDestination(
        route = "agent_card_enroll",
        label = "Enrôlement carte",
    )

    data object AgentCardAdd : KoriDestination(
        route = "agent_card_add",
        label = "Ajout carte",
    )

    data object AgentCardStatusUpdate : KoriDestination(
        route = "agent_card_status_update",
        label = "Statut carte",
    )

    data object AgentSearch : KoriDestination(
        route = "agent_search",
        label = "Recherche",
    )

    data object Action : KoriDestination(
        route = "action",
        label = "Action",
    )

    data object Activity : KoriDestination(
        route = "activity",
        label = "Activités",
    )


    data object ClientCards : KoriDestination(
        route = "client_cards",
        label = "Mes cartes",
    )

    data object Profile : KoriDestination(
        route = "profile",
        label = "Profil",
    )

    data object Session : KoriDestination(
        route = "session",
        label = "Session",
    )
}
