package com.kori.app.feature.action

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kori.app.R
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.component.ConfirmModal
import com.kori.app.core.designsystem.component.DetailRow
import com.kori.app.core.designsystem.component.ScreenHeader
import com.kori.app.core.designsystem.component.SectionCard
import com.kori.app.core.designsystem.component.SuccessReceiptSheet
import com.kori.app.core.model.action.AgentCardTargetStatus

@Composable
fun AgentCardStatusUpdateScreen(
    uiState: AgentCardStatusUpdateUiState,
    onCardUidChanged: (String) -> Unit,
    onTargetStatusChanged: (AgentCardTargetStatus) -> Unit,
    onReasonChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onConfirmSubmit: () -> Unit,
    onDismissConfirm: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is AgentCardStatusUpdateUiState.Form -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { ScreenHeader(stringResource(R.string.card_status_form_title), stringResource(R.string.card_status_form_subtitle)) }
                item {
                    SectionCard(
                        title = stringResource(R.string.card_status_section_title),
                        subtitle = stringResource(R.string.card_status_section_subtitle),
                    ) {
                        OutlinedTextField(
                            value = uiState.draft.cardUid,
                            onValueChange = onCardUidChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.card_status_card_label)) },
                            placeholder = { Text(stringResource(R.string.card_status_card_placeholder)) },
                            singleLine = true,
                            isError = uiState.errors.cardUid != null,
                            supportingText = { uiState.errors.cardUid?.let { Text(it) } },
                        )
                        CompactStatusSelector(
                            selectedStatus = uiState.draft.targetStatus,
                            onStatusSelected = onTargetStatusChanged,
                            error = uiState.errors.targetStatus,
                        )
                        OutlinedTextField(
                            value = uiState.draft.reason,
                            onValueChange = onReasonChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.card_status_reason_label)) },
                            placeholder = { Text(stringResource(R.string.card_status_reason_placeholder)) },
                            minLines = 3,
                            maxLines = 4,
                            isError = uiState.errors.reason != null,
                            supportingText = {
                                val error = uiState.errors.reason
                                if (error != null) Text(error) else Text("${uiState.draft.reason.length}/255")
                            },
                        )
                    }
                }
                item {
                    Button(onClick = onSubmit, enabled = !uiState.isSubmitting, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary), contentPadding = PaddingValues(vertical = 16.dp)) {
                        if (uiState.isSubmitting) CircularProgressIndicator(strokeWidth = 2.dp) else Text(stringResource(R.string.common_continue))
                    }
                }
            }

            if (uiState.showConfirmModal) {
                val statusLabel = when (uiState.draft.targetStatus) {
                    AgentCardTargetStatus.BLOCKED -> stringResource(R.string.card_status_block)
                    AgentCardTargetStatus.LOST -> stringResource(R.string.card_status_lost)
                    null -> "-"
                }
                ConfirmModal(
                    title = stringResource(R.string.card_status_confirm_title),
                    message = stringResource(R.string.card_status_confirm_message, uiState.draft.cardUid, statusLabel),
                    confirmLabel = stringResource(R.string.flow_check_confirm),
                    dismissLabel = stringResource(R.string.common_cancel),
                    onConfirm = onConfirmSubmit,
                    onDismiss = onDismissConfirm,
                )
            }
        }

        is AgentCardStatusUpdateUiState.Success -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ScreenHeader(stringResource(R.string.card_status_success_title), stringResource(R.string.card_status_success_subtitle)) }
            item {
                SuccessReceiptSheet(
                    title = stringResource(R.string.common_result),
                    lines = listOf(
                        stringResource(R.string.card_enroll_card) to uiState.receipt.subjectRef,
                        stringResource(R.string.card_status_previous) to uiState.receipt.previousStatus,
                        stringResource(R.string.card_status_new) to uiState.receipt.newStatus,
                    ),
                )
            }
            item {
                Button(onClick = onRestart, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp), colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary)) {
                    Text(stringResource(R.string.card_status_restart))
                }
            }
        }

        is AgentCardStatusUpdateUiState.Failure -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ScreenHeader(stringResource(R.string.card_status_failure_title), stringResource(R.string.card_status_failure_subtitle)) }
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

@Composable
private fun CompactStatusSelector(
    selectedStatus: AgentCardTargetStatus?,
    onStatusSelected: (AgentCardTargetStatus) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.card_status_action_label),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusOptionCard(
                label = stringResource(R.string.card_status_block),
                selected = selectedStatus == AgentCardTargetStatus.BLOCKED,
                onClick = { onStatusSelected(AgentCardTargetStatus.BLOCKED) },
                modifier = Modifier.weight(1f),
            )
            StatusOptionCard(
                label = stringResource(R.string.card_status_lost),
                selected = selectedStatus == AgentCardTargetStatus.LOST,
                onClick = { onStatusSelected(AgentCardTargetStatus.LOST) },
                modifier = Modifier.weight(1f),
            )
        }

        error?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun StatusOptionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)
    val borderColor = if (selected) KoriAccent else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (selected) KoriAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

