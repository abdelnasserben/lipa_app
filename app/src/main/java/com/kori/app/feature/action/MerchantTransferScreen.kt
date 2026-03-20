package com.kori.app.feature.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kori.app.R
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.component.ConfirmModal
import com.kori.app.core.designsystem.component.DetailRow
import com.kori.app.core.designsystem.component.ScreenHeader
import com.kori.app.core.designsystem.component.SectionCard
import com.kori.app.core.designsystem.component.SuccessReceiptSheet
import com.kori.app.core.ui.formatIsoToDisplay
import com.kori.app.core.ui.formatKmf

@Composable
fun MerchantTransferScreen(
    uiState: MerchantTransferUiState,
    onRecipientChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onContinue: () -> Unit,
    onOpenConfirmDialog: () -> Unit,
    onDismissConfirmDialog: () -> Unit,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources

    when (uiState) {
        is MerchantTransferUiState.Form -> MerchantTransferFormContent(uiState, onRecipientChanged, onAmountChanged, onContinue, modifier)
        is MerchantTransferUiState.Confirmation -> MerchantTransferConfirmationContent(uiState, onOpenConfirmDialog, onDismissConfirmDialog, onConfirm, onEdit, modifier)
        is MerchantTransferUiState.Success -> MerchantTransferSuccessContent(uiState, onRestart, modifier)
        is MerchantTransferUiState.Failure -> MerchantTransferFailureContent(uiState, onRestart, modifier)
    }
}

@Composable
private fun MerchantTransferFormContent(
    state: MerchantTransferUiState.Form,
    onRecipientChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader(stringResource(R.string.merchant_transfer_form_title), stringResource(R.string.merchant_transfer_form_subtitle)) }
        item {
            SectionCard(title = stringResource(R.string.merchant_transfer_section)) {
                OutlinedTextField(
                    value = state.draft.recipientMerchantCode,
                    onValueChange = onRecipientChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.merchant_transfer_recipient_label)) },
                    placeholder = { Text(stringResource(R.string.merchant_transfer_recipient_placeholder)) },
                    singleLine = true,
                    isError = state.errors.recipientMerchantCode != null,
                    supportingText = { state.errors.recipientMerchantCode?.let { Text(it) } },
                )
                OutlinedTextField(
                    value = state.draft.amountInput,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.common_amount)) },
                    placeholder = { Text(stringResource(R.string.merchant_transfer_amount_placeholder)) },
                    singleLine = true,
                    isError = state.errors.amount != null,
                    supportingText = { state.errors.amount?.let { Text(it) } },
                )
            }
        }
        item {
            Button(
                onClick = onContinue,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary),
            ) {
                if (state.isLoading) CircularProgressIndicator(strokeWidth = 2.dp) else Text(stringResource(R.string.common_continue))
            }
        }
    }
}

@Composable
private fun MerchantTransferConfirmationContent(
    state: MerchantTransferUiState.Confirmation,
    onOpenConfirmDialog: () -> Unit,
    onDismissConfirmDialog: () -> Unit,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader(stringResource(R.string.merchant_transfer_confirm_title), stringResource(R.string.merchant_transfer_confirm_subtitle)) }
        item {
            SectionCard(title = stringResource(R.string.common_details)) {
                DetailRow(label = stringResource(R.string.merchant_transfer_recipient), value = state.model.quote.recipientMerchantCode)
                DetailRow(label = stringResource(R.string.common_amount), value = formatKmf(resources, state.model.quote.amount))
                DetailRow(label = stringResource(R.string.common_fees), value = formatKmf(resources, state.model.quote.fee))
                DetailRow(label = stringResource(R.string.merchant_transfer_total), value = formatKmf(resources, state.model.quote.totalDebited), showDivider = false)
            }
        }
        item {
            OutlinedButton(onClick = onEdit, enabled = !state.model.isSubmitting, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp)) {
                Text(stringResource(R.string.common_edit))
            }
        }
        item {
            Button(
                onClick = onOpenConfirmDialog,
                enabled = !state.model.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary),
            ) {
                if (state.model.isSubmitting) CircularProgressIndicator(strokeWidth = 2.dp) else Text(stringResource(R.string.merchant_transfer_confirm_button))
            }
        }
    }
    if (state.model.isConfirmDialogVisible) {
        ConfirmModal(
            title = stringResource(R.string.flow_check_title),
            message = stringResource(R.string.merchant_transfer_confirm_message),
            confirmLabel = stringResource(R.string.flow_check_confirm),
            dismissLabel = stringResource(R.string.common_cancel),
            onConfirm = onConfirm,
            onDismiss = onDismissConfirmDialog,
        )
    }
}

@Composable
private fun MerchantTransferSuccessContent(
    state: MerchantTransferUiState.Success,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader(stringResource(R.string.merchant_transfer_success_title), stringResource(R.string.merchant_transfer_success_subtitle)) }
        item {
            SuccessReceiptSheet(
                title = stringResource(R.string.merchant_transfer_receipt_title),
                lines = listOf(
                    stringResource(R.string.common_reference) to state.model.receipt.transactionRef,
                    stringResource(R.string.merchant_transfer_recipient) to state.model.receipt.recipientMerchantCode,
                    stringResource(R.string.common_amount) to formatKmf(resources, state.model.receipt.amount),
                    stringResource(R.string.common_fees) to formatKmf(resources, state.model.receipt.fee),
                    stringResource(R.string.merchant_transfer_total) to formatKmf(resources, state.model.receipt.totalDebited),
                    stringResource(R.string.common_date) to formatIsoToDisplay(resources, state.model.receipt.createdAt),
                ),
            )
        }
        item {
            Button(onClick = onRestart, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary)) {
                Text(stringResource(R.string.merchant_transfer_restart))
            }
        }
    }
}

@Composable
private fun MerchantTransferFailureContent(
    state: MerchantTransferUiState.Failure,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader(stringResource(R.string.merchant_transfer_failure_title), stringResource(R.string.merchant_transfer_failure_subtitle)) }
        item {
            SectionCard(title = stringResource(R.string.common_details)) {
                DetailRow(label = stringResource(R.string.common_message), value = state.model.userMessage, showDivider = false)
            }
        }
        item {
            Button(onClick = onRestart, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary)) {
                Text(stringResource(R.string.common_retry))
            }
        }
    }
}
