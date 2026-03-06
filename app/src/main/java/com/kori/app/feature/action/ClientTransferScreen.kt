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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.KoriSurface
import com.kori.app.core.designsystem.component.ConfirmModal
import com.kori.app.core.designsystem.component.SuccessReceiptSheet
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
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ActionHeader(
                title = "Envoyer à un proche",
                subtitle = "Saisissez le numéro du bénéficiaire et le montant à transférer.",
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = KoriSurface),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    OutlinedTextField(
                        value = state.draft.recipientPhoneNumber,
                        onValueChange = onRecipientChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Numéro du bénéficiaire") },
                        placeholder = { Text("+269 3xx xx xx") },
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
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ActionHeader(
                title = "Confirmer le transfert",
                subtitle = "Vérifiez les informations avant d’exécuter l’opération.",
            )
        }

        item {
            SummaryCard(
                lines = listOf(
                    "Bénéficiaire" to state.quote.recipientPhoneNumber,
                    "Montant" to formatKmf(state.quote.amount),
                    "Frais" to formatKmf(state.quote.fee),
                    "Total débité" to formatKmf(state.quote.totalDebited),
                ),
            )
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
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ActionHeader(
                title = "Transfert envoyé",
                subtitle = "L’opération a été enregistrée avec succès.",
            )
        }

        item {
            SuccessReceiptSheet(
                title = "Reçu de transfert",
                lines = listOf(
                    "Référence" to state.receipt.transactionRef,
                    "Bénéficiaire" to state.receipt.recipientPhoneNumber,
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
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ActionHeader(
                title = "Transfert non abouti",
                subtitle = "L’opération n’a pas pu être finalisée.",
            )
        }

        item {
            SummaryCard(
                lines = listOf(
                    "Code erreur" to state.code.name,
                    "Message" to state.message,
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
                Text("Réessayer")
            }
        }
    }
}

@Composable
fun ActionHeader(
    title: String,
    subtitle: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = KoriPrimary,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SummaryCard(
    lines: List<Pair<String, String>>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            lines.forEach { (label, value) ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
fun DebugPanel(
    lines: List<String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = KoriSurface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Debug",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}