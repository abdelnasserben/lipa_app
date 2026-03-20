package com.kori.app.navigation

import androidx.annotation.StringRes
import com.kori.app.R

sealed class KoriDestination(
    val route: String,
    @StringRes val labelResId: Int,
) {
    data object RolePicker : KoriDestination(
        route = "role_picker",
        labelResId = R.string.nav_home,
    )

    data object AuthWelcome : KoriDestination(
        route = "auth_welcome",
        labelResId = R.string.destination_auth_welcome,
    )

    data object AuthCallback : KoriDestination(
        route = "auth_callback",
        labelResId = R.string.destination_auth_callback,
    )

    data object AuthSuccess : KoriDestination(
        route = "auth_success",
        labelResId = R.string.destination_auth_success,
    )

    data object Dashboard : KoriDestination(
        route = "dashboard",
        labelResId = R.string.destination_dashboard,
    )

    data object Transactions : KoriDestination(
        route = "transactions",
        labelResId = R.string.nav_transactions,
    )

    data object TransactionDetail : KoriDestination(
        route = "transaction_detail/{transactionRef}",
        labelResId = R.string.nav_transaction_detail,
    ) {
        fun createRoute(transactionRef: String): String {
            return "transaction_detail/$transactionRef"
        }
    }

    data object ClientTransfer : KoriDestination(
        route = "client_transfer",
        labelResId = R.string.destination_client_transfer,
    )

    data object MerchantTransfer : KoriDestination(
        route = "merchant_transfer",
        labelResId = R.string.nav_merchant_transfer,
    )

    data object AgentCashIn : KoriDestination(
        route = "agent_cash_in",
        labelResId = R.string.destination_agent_cash_in,
    )

    data object AgentMerchantWithdraw : KoriDestination(
        route = "agent_merchant_withdraw",
        labelResId = R.string.nav_agent_withdraw,
    )

    data object AgentCardEnroll : KoriDestination(
        route = "agent_card_enroll",
        labelResId = R.string.destination_agent_card_enroll,
    )

    data object AgentCardAdd : KoriDestination(
        route = "agent_card_add",
        labelResId = R.string.destination_agent_card_add,
    )

    data object AgentCardStatusUpdate : KoriDestination(
        route = "agent_card_status_update",
        labelResId = R.string.nav_agent_card_status,
    )

    data object AgentSearch : KoriDestination(
        route = "agent_search",
        labelResId = R.string.nav_agent_search,
    )

    data object Action : KoriDestination(
        route = "action",
        labelResId = R.string.destination_action,
    )

    data object Activity : KoriDestination(
        route = "activity",
        labelResId = R.string.role_shared_history,
    )

    data object ClientCards : KoriDestination(
        route = "client_cards",
        labelResId = R.string.nav_client_cards,
    )

    data object Profile : KoriDestination(
        route = "profile",
        labelResId = R.string.nav_profile,
    )
}
