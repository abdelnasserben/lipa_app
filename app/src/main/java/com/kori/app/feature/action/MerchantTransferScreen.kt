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
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.component.ConfirmModal
import com.kori.app.core.designsystem.component.DebugPanel
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
    when (uiState) {
        is MerchantTransferUiState.Form -> {
            MerchantTransferFormContent(
                state = uiState,
                onRecipientChanged = onRecipientChanged,
                onAmountChanged = onAmountChanged,
                onContinue = onContinue,
                modifier = modifier,
            )
        }

        is MerchantTransferUiState.Confirmation -> {
            MerchantTransferConfirmationContent(
                state = uiState,
                onOpenConfirmDialog = onOpenConfirmDialog,
                onDismissConfirmDialog = onDismissConfirmDialog,
                onConfirm = onConfirm,
                onEdit = onEdit,
                modifier = modifier,
            )
        }

        is MerchantTransferUiState.Success -> {
            MerchantTransferSuccessContent(
                state = uiState,
                onRestart = onRestart,
                modifier = modifier,
            )
        }

        is MerchantTransferUiState.Failure -> {
            MerchantTransferFailureContent(
                state = uiState,
                onRestart = onRestart,
                modifier = modifier,
            )
        }
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
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Transfert marchand",
                subtitle = "Saisissez le code marchand bénéficiaire et le montant à transférer.",
            )
        }

        item {
            SectionCard(title = "Informations de transfert") {
                OutlinedTextField(
                    value = state.draft.recipientMerchantCode,
                    onValueChange = onRecipientChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Code marchand bénéficiaire") },
                    placeholder = { Text("Ex. MER-0088") },
                    singleLine = true,
                    isError = state.errors.recipientMerchantCode != null,
                    supportingText = {
                        state.errors.recipientMerchantCode?.let { Text(it) }
                    },
                )

                OutlinedTextField(
                    value = state.draft.amountInput,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Montant") },
                    placeholder = { Text("Ex. 75000") },
                    singleLine = true,
                    isError = state.errors.amount != null,
                    supportingText = {
                        state.errors.amount?.let { Text(it) }
                    },
                )
            }
        }

        item {
            DebugPanel(
                lines = listOf(
                    "Le quote générera une Idempotency-Key avant confirmation.",
                ),
            )
        }

        item {
            Button(
                onClick = onContinue,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KoriAccent,
                    contentColor = KoriPrimary,
                ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Continuer")
                }
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
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Confirmer le transfert marchand",
                subtitle = "Vérifiez les informations avant d’exécuter l’opération.",
            )
        }

        item {
            SectionCard(title = "Récapitulatif") {
                DetailRow(label = "Marchand bénéficiaire", value = state.quote.recipientMerchantCode)
                DetailRow(label = "Montant", value = formatKmf(state.quote.amount))
                DetailRow(label = "Frais", value = formatKmf(state.quote.fee))
                DetailRow(label = "Total débité", value = formatKmf(state.quote.totalDebited), showDivider = false)
            }
        }

        item {
            DebugPanel(
                lines = listOf(
                    "Idempotency-Key",
                    state.quote.idempotencyKey,
                ),
            )
        }

        item {
            OutlinedButton(
                onClick = onEdit,
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text("Modifier")
            }
        }

        item {
            Button(
                onClick = onOpenConfirmDialog,
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KoriAccent,
                    contentColor = KoriPrimary,
                ),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Confirmer le transfert")
                }
            }
        }
    }

    if (state.isConfirmDialogVisible) {
        ConfirmModal(
            title = "Dernière vérification",
            message = "Cette opération va débiter votre solde marchand. Assurez-vous que le code marchand bénéficiaire et le montant sont corrects.",
            confirmLabel = "Je confirme",
            dismissLabel = "Annuler",
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
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Transfert marchand envoyé",
                subtitle = "L’opération a été enregistrée avec succès.",
            )
        }

        item {
            SuccessReceiptSheet(
                title = "Reçu de transfert marchand",
                lines = listOf(
                    "Référence" to state.receipt.transactionRef,
                    "Marchand bénéficiaire" to state.receipt.recipientMerchantCode,
                    "Montant" to formatKmf(state.receipt.amount),
                    "Frais" to formatKmf(state.receipt.fee),
                    "Total débité" to formatKmf(state.receipt.totalDebited),
                    "Date" to formatIsoToDisplay(state.receipt.createdAt),
                ),
            )
        }

        item {
            DebugPanel(
                lines = listOf(
                    "Idempotency-Key",
                    state.idempotencyKey,
                ),
            )
        }

        item {
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KoriAccent,
                    contentColor = KoriPrimary,
                ),
            ) {
                Text("Nouveau transfert")
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
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Transfert marchand non abouti",
                subtitle = "L’opération n’a pas pu être finalisée.",
            )
        }

        item {
            SectionCard(title = "Détails") {
                DetailRow(label = "Code erreur", value = state.code.name)
                DetailRow(label = "Message", value = state.message, showDivider = false)
            }
        }

        item {
            DebugPanel(
                lines = listOf(
                    "Idempotency-Key",
                    state.idempotencyKey,
                ),
            )
        }

        item {
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KoriAccent,
                    contentColor = KoriPrimary,
                ),
            ) {
                Text("Réessayer")
            }
        }
    }
}