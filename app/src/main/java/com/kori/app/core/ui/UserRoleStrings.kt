package com.kori.app.core.ui

import androidx.annotation.StringRes
import com.kori.app.R
import com.kori.app.core.model.UserRole

@StringRes
fun UserRole.labelResId(): Int = when (this) {
    UserRole.CLIENT -> R.string.role_client
    UserRole.MERCHANT -> R.string.role_merchant
    UserRole.AGENT -> R.string.role_agent
}

@StringRes
fun UserRole.dashboardTitleResId(): Int = when (this) {
    UserRole.CLIENT -> R.string.role_client_dashboard
    UserRole.MERCHANT -> R.string.role_merchant_dashboard
    UserRole.AGENT -> R.string.role_agent_dashboard
}

@StringRes
fun UserRole.actionLabelResId(): Int = when (this) {
    UserRole.CLIENT -> R.string.role_client_action
    UserRole.MERCHANT -> R.string.role_merchant_action
    UserRole.AGENT -> R.string.role_agent_action
}

@StringRes
fun UserRole.historyLabelResId(): Int = when (this) {
    UserRole.CLIENT -> R.string.role_client_history
    UserRole.MERCHANT,
    UserRole.AGENT -> R.string.role_shared_history
}
