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
import com.kori.app.core.ui.FinancialInputRules
import com.kori.app.core.ui.formatIsoToDisplay
import com.kori.app.core.ui.formatKmf

@Composable
fun ClientTransferScreen(
    uiState: ClientTransferUiState,
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
        is ClientTransferUiState.Form -> {
            ClientTransferFormContent(
                state = uiState,
                onRecipientChanged = onRecipientChanged,
                onAmountChanged = onAmountChanged,
                onContinue = onContinue,
                modifier = modifier,
            )
        }

        is ClientTransferUiState.Confirmation -> {
            ClientTransferConfirmationContent(
                state = uiState,
                onOpenConfirmDialog = onOpenConfirmDialog,
                onDismissConfirmDialog = onDismissConfirmDialog,
                onConfirm = onConfirm,
                onEdit = onEdit,
                modifier = modifier,
            )
        }

        is ClientTransferUiState.Success -> {
            ClientTransferSuccessContent(
                state = uiState,
                onRestart = onRestart,
                modifier = modifier,
            )
        }

        is ClientTransferUiState.Failure -> {
            ClientTransferFailureContent(
                state = uiState,
                onRestart = onRestart,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ClientTransferFormContent(
    state: ClientTransferUiState.Form,
    onRecipientChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 92.dp,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Envoyer à un proche",
                subtitle = "Saisissez le numéro du bénéficiaire et le montant à transférer.",
            )
        }

        item {
            SectionCard(title = "Informations de transfert") {
                OutlinedTextField(
                    value = state.draft.recipientPhoneNumber,
                    onValueChange = onRecipientChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Numéro du bénéficiaire") },
                    placeholder = { Text("+269 3XX XX XX") },
                    singleLine = true,
                    isError = state.errors.recipientPhoneNumber != null,
                    supportingText = {
                        state.errors.recipientPhoneNumber?.let { Text(it) }
                    },
                )

                OutlinedTextField(
                    value = state.draft.amountInput,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Montant") },
                    placeholder = { Text("Ex. 15000") },
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
                    if (state.isLoading) "Réseau mock: préparation en cours" else "Réseau mock: prêt",
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
private fun ClientTransferConfirmationContent(
    state: ClientTransferUiState.Confirmation,
    onOpenConfirmDialog: () -> Unit,
    onDismissConfirmDialog: () -> Unit,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 92.dp,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Confirmer le transfert",
                subtitle = "Vérifiez les informations avant d’exécuter l’opération.",
            )
        }

        item {
            SectionCard(title = "Récapitulatif") {
                DetailRow(label = "Bénéficiaire", value = FinancialInputRules.formatComorosPhoneForDisplay(state.quote.recipientPhoneNumber))
                DetailRow(label = "Montant", value = formatKmf(state.quote.amount))
                DetailRow(label = "Frais", value = formatKmf(state.quote.fee))
                DetailRow(
                    label = "Total débité",
                    value = formatKmf(state.quote.totalDebited),
                    showDivider = false,
                )
            }
        }

        item {
            DebugPanel(
                lines = listOf(
                    "Idempotency-Key",
                    state.quote.idempotencyKey,
                    if (state.isSubmitting) "Réseau mock: soumission en cours" else "Réseau mock: en attente de confirmation",
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
            message = "Cette opération financière va débiter votre solde principal. Assurez-vous que le numéro et le montant sont corrects.",
            confirmLabel = "Je confirme",
            dismissLabel = "Annuler",
            onConfirm = onConfirm,
            onDismiss = onDismissConfirmDialog,
        )
    }
}

@Composable
private fun ClientTransferSuccessContent(
    state: ClientTransferUiState.Success,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 92.dp,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Transfert envoyé",
                subtitle = "L’opération a été enregistrée avec succès.",
            )
        }

        item {
            SuccessReceiptSheet(
                title = "Reçu de transfert",
                lines = listOf(
                    "Référence" to state.receipt.transactionRef,
                    "Bénéficiaire" to FinancialInputRules.formatComorosPhoneForDisplay(state.receipt.recipientPhoneNumber),
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
private fun ClientTransferFailureContent(
    state: ClientTransferUiState.Failure,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 92.dp,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Transfert non abouti",
                subtitle = "L’opération n’a pas pu être finalisée.",
            )
        }

        item {
            SectionCard(title = "Détails") {
                DetailRow(label = "Code erreur", value = state.code.name)
                DetailRow(label = "Message", value = state.userMessage, showDivider = false)
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
