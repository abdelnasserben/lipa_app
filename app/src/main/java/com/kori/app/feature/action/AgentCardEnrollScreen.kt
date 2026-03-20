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
import androidx.compose.ui.platform.LocalContext
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
import com.kori.app.core.ui.FinancialInputRules
import com.kori.app.core.ui.formatKmf

@Composable
fun AgentCardEnrollScreen(
    uiState: AgentCardEnrollUiState,
    onPhoneChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onCardUidChanged: (String) -> Unit,
    onPinChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources

    when (uiState) {
        is AgentCardEnrollUiState.Form -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ScreenHeader(stringResource(R.string.card_enroll_form_title), stringResource(R.string.card_enroll_form_subtitle)) }
            item {
                SectionCard(title = stringResource(R.string.card_enroll_section)) {
                    OutlinedTextField(
                        value = uiState.draft.phoneNumber,
                        onValueChange = onPhoneChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.card_enroll_phone_label)) },
                        placeholder = { Text(stringResource(R.string.client_transfer_recipient_placeholder)) },
                        singleLine = true,
                        isError = uiState.errors.phoneNumber != null,
                        supportingText = { uiState.errors.phoneNumber?.let { Text(it) } },
                    )
                    OutlinedTextField(
                        value = uiState.draft.displayName,
                        onValueChange = onDisplayNameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.card_enroll_name_label)) },
                        singleLine = true,
                        isError = uiState.errors.displayName != null,
                        supportingText = { uiState.errors.displayName?.let { Text(it) } },
                    )
                    OutlinedTextField(
                        value = uiState.draft.cardUid,
                        onValueChange = onCardUidChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.card_enroll_card_label)) },
                        singleLine = true,
                        isError = uiState.errors.cardUid != null,
                        supportingText = { uiState.errors.cardUid?.let { Text(it) } },
                    )
                    OutlinedTextField(
                        value = uiState.draft.pin,
                        onValueChange = onPinChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.card_enroll_pin_label)) },
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

        is AgentCardEnrollUiState.Success -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ScreenHeader(stringResource(R.string.card_enroll_success_title), stringResource(R.string.card_enroll_success_subtitle)) }
            item {
                SuccessReceiptSheet(
                    title = stringResource(R.string.card_enroll_receipt_title),
                    lines = listOf(
                        stringResource(R.string.card_enroll_transaction) to uiState.receipt.transactionId,
                        stringResource(R.string.card_enroll_client) to uiState.receipt.clientCode,
                        stringResource(R.string.card_enroll_phone) to FinancialInputRules.formatComorosPhoneForDisplay(uiState.receipt.clientPhoneNumber),
                        stringResource(R.string.card_enroll_card) to uiState.receipt.cardUid,
                        stringResource(R.string.card_enroll_price) to formatKmf(resources, uiState.receipt.cardPrice),
                        stringResource(R.string.common_commission) to formatKmf(resources, uiState.receipt.agentCommission),
                    ),
                )
            }
            item {
                SectionCard(title = stringResource(R.string.card_enroll_status_title)) {
                    DetailRow(label = stringResource(R.string.card_enroll_client_created), value = if (uiState.receipt.clientCreated) stringResource(R.string.common_yes) else stringResource(R.string.common_no))
                    DetailRow(
                        label = stringResource(R.string.card_enroll_profile_created),
                        value = if (uiState.receipt.clientAccountProfileCreated) stringResource(R.string.common_yes) else stringResource(R.string.common_no),
                        showDivider = false,
                    )
                }
            }
            item {
                Button(onClick = onRestart, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary)) {
                    Text(stringResource(R.string.card_enroll_restart))
                }
            }
        }

        is AgentCardEnrollUiState.Failure -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ScreenHeader(stringResource(R.string.card_enroll_failure_title), stringResource(R.string.card_enroll_failure_subtitle)) }
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
