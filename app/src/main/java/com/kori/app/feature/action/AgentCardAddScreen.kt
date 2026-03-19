package com.kori.app.feature.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kori.app.R
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.component.DetailRow
import com.kori.app.core.designsystem.component.ScreenHeader
import com.kori.app.core.designsystem.component.SectionCard
import com.kori.app.core.designsystem.component.SuccessReceiptSheet
import com.kori.app.core.ui.formatKmf

@Composable
fun AgentCardAddScreen(
    uiState: AgentCardAddUiState,
    onPhoneChanged: (String) -> Unit,
    onCardUidChanged: (String) -> Unit,
    onPinChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is AgentCardAddUiState.Form -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ScreenHeader(stringResource(R.string.card_add_form_title), stringResource(R.string.card_add_form_subtitle)) }
            item {
                SectionCard(title = stringResource(R.string.card_add_section)) {
                    OutlinedTextField(
                        value = uiState.draft.phoneNumber,
                        onValueChange = onPhoneChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.card_add_phone_label)) },
                        placeholder = { Text(stringResource(R.string.client_transfer_recipient_placeholder)) },
                        singleLine = true,
                        isError = uiState.errors.phoneNumber != null,
                        supportingText = { uiState.errors.phoneNumber?.let { Text(it) } },
                    )
                    OutlinedTextField(
                        value = uiState.draft.cardUid,
                        onValueChange = onCardUidChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.card_add_card_label)) },
                        singleLine = true,
                        isError = uiState.errors.cardUid != null,
                        supportingText = { uiState.errors.cardUid?.let { Text(it) } },
                    )
                    OutlinedTextField(
                        value = uiState.draft.pin,
                        onValueChange = onPinChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.card_add_pin_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = uiState.errors.pin != null,
                        supportingText = { uiState.errors.pin?.let { Text(it) } },
                    )
                }
            }
            item {
                Button(onClick = onSubmit, enabled = !uiState.isSubmitting, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary)) {
                    if (uiState.isSubmitting) CircularProgressIndicator(strokeWidth = 2.dp) else Text(stringResource(R.string.common_submit))
                }
            }
        }

        is AgentCardAddUiState.Success -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ScreenHeader(stringResource(R.string.card_add_success_title), stringResource(R.string.card_add_success_subtitle)) }
            item {
                SuccessReceiptSheet(
                    title = stringResource(R.string.card_add_receipt_title),
                    lines = listOf(
                        stringResource(R.string.card_add_transaction) to uiState.receipt.transactionId,
                        stringResource(R.string.card_add_client) to uiState.receipt.clientId,
                        stringResource(R.string.card_add_card) to uiState.receipt.cardUid,
                        stringResource(R.string.card_add_price) to formatKmf(uiState.receipt.cardPrice),
                        stringResource(R.string.common_commission) to formatKmf(uiState.receipt.agentCommission),
                    ),
                )
            }
            item {
                Button(onClick = onRestart, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary)) {
                    Text(stringResource(R.string.card_add_restart))
                }
            }
        }

        is AgentCardAddUiState.Failure -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ScreenHeader(stringResource(R.string.card_add_failure_title), stringResource(R.string.card_add_failure_subtitle)) }
            item {
                SectionCard(title = stringResource(R.string.common_details)) {
                    DetailRow(label = stringResource(R.string.common_message), value = uiState.userMessage, showDivider = false)
                }
            }
            item {
                Button(onClick = onRestart, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary)) {
                    Text(stringResource(R.string.common_retry))
                }
            }
        }
    }
}
