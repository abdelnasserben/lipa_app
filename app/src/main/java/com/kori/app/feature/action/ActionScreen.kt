package com.kori.app.feature.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.R
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurface
import com.kori.app.core.model.UserRole

@Composable
fun ActionScreen(
    role: UserRole,
    onOpenClientTransfer: () -> Unit,
    onOpenMerchantTransfer: () -> Unit,
    onOpenAgentCashIn: () -> Unit,
    onOpenAgentMerchantWithdraw: () -> Unit,
    onOpenAgentCardEnroll: () -> Unit,
    onOpenAgentCardAdd: () -> Unit,
    onOpenAgentCardStatusUpdate: () -> Unit,
    onOpenAgentSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (role) {
        UserRole.CLIENT -> {
            ClientActionHome(
                onOpenClientTransfer = onOpenClientTransfer,
                modifier = modifier,
            )
        }

        UserRole.MERCHANT -> {
            MerchantActionHome(
                onOpenMerchantTransfer = onOpenMerchantTransfer,
                modifier = modifier,
            )
        }

        UserRole.AGENT -> {
            AgentActionHome(
                onOpenCashIn = onOpenAgentCashIn,
                onOpenMerchantWithdraw = onOpenAgentMerchantWithdraw,
                onOpenCardEnroll = onOpenAgentCardEnroll,
                onOpenCardAdd = onOpenAgentCardAdd,
                onOpenCardStatusUpdate = onOpenAgentCardStatusUpdate,
                onOpenAgentSearch = onOpenAgentSearch,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ClientActionHome(
    onOpenClientTransfer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.action_client_home_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = KoriPrimary,
            )
        }

        item {
            ActionEntryCard(
                title = stringResource(R.string.action_client_transfer_title),
                message = stringResource(R.string.action_client_transfer_message),
                cta = stringResource(R.string.action_start),
                onClick = onOpenClientTransfer,
            )
        }
    }
}

@Composable
private fun MerchantActionHome(
    onOpenMerchantTransfer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.action_merchant_home_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = KoriPrimary,
            )
        }

        item {
            ActionEntryCard(
                title = stringResource(R.string.action_merchant_transfer_title),
                message = stringResource(R.string.action_merchant_transfer_message),
                cta = stringResource(R.string.action_start),
                onClick = onOpenMerchantTransfer,
            )
        }
    }
}

@Composable
private fun AgentActionHome(
    onOpenCashIn: () -> Unit,
    onOpenMerchantWithdraw: () -> Unit,
    onOpenCardEnroll: () -> Unit,
    onOpenCardAdd: () -> Unit,
    onOpenCardStatusUpdate: () -> Unit,
    onOpenAgentSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.action_agent_home_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = KoriPrimary,
            )
        }

        item {
            ActionEntryCard(
                title = stringResource(R.string.action_agent_cash_in_title),
                message = stringResource(R.string.action_agent_cash_in_message),
                cta = stringResource(R.string.action_start),
                onClick = onOpenCashIn,
            )
        }

        item {
            ActionEntryCard(
                title = stringResource(R.string.action_agent_withdraw_title),
                message = stringResource(R.string.action_agent_withdraw_message),
                cta = stringResource(R.string.action_start),
                onClick = onOpenMerchantWithdraw,
            )
        }

        item {
            ActionEntryCard(
                title = stringResource(R.string.action_agent_card_enroll_title),
                message = stringResource(R.string.action_agent_card_enroll_message),
                cta = stringResource(R.string.action_start),
                onClick = onOpenCardEnroll,
            )
        }

        item {
            ActionEntryCard(
                title = stringResource(R.string.action_agent_card_add_title),
                message = stringResource(R.string.action_agent_card_add_message),
                cta = stringResource(R.string.action_start),
                onClick = onOpenCardAdd,
            )
        }

        item {
            ActionEntryCard(
                title = stringResource(R.string.action_agent_card_status_title),
                message = stringResource(R.string.action_agent_card_status_message),
                cta = stringResource(R.string.action_start),
                onClick = onOpenCardStatusUpdate,
            )
        }

        item {
            ActionEntryCard(
                title = stringResource(R.string.action_agent_search_title),
                message = stringResource(R.string.action_agent_search_message),
                cta = stringResource(R.string.action_open_search),
                onClick = onOpenAgentSearch,
            )
        }
    }
}

@Composable
private fun ActionEntryCard(
    title: String,
    message: String,
    cta: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KoriAccent,
                    contentColor = KoriPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Text(cta)
            }
        }
    }
}
