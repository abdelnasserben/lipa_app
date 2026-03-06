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
fun AgentCashInScreen(
    uiState: AgentCashInUiState,
    onPhoneChanged: (String) -> Unit,
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
        is AgentCashInUiState.Form -> AgentCashInFormContent(
            state = uiState,
            onPhoneChanged = onPhoneChanged,
            onAmountChanged = onAmountChanged,
            onContinue = onContinue,
            modifier = modifier,
        )

        is AgentCashInUiState.Confirmation -> AgentCashInConfirmationContent(
            state = uiState,
            onOpenConfirmDialog = onOpenConfirmDialog,
            onDismissConfirmDialog = onDismissConfirmDialog,
            onConfirm = onConfirm,
            onEdit = onEdit,
            modifier = modifier,
        )

        is AgentCashInUiState.Success -> AgentCashInSuccessContent(
            state = uiState,
            onRestart = onRestart,
            modifier = modifier,
        )

        is AgentCashInUiState.Failure -> AgentCashInFailureContent(
            state = uiState,
            onRestart = onRestart,
            modifier = modifier,
        )
    }
}

@Composable
private fun AgentCashInFormContent(
    state: AgentCashInUiState.Form,
    onPhoneChanged: (String) -> Unit,
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
                title = "Cash-in client",
                subtitle = "Saisissez le numéro du client et le montant à créditer.",
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
                        value = state.draft.phoneNumber,
                        onValueChange = onPhoneChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Numéro du client") },
                        placeholder = { Text("+269 3xx xx xx") },
                        singleLine = true,
                        isError = state.errors.phoneNumber != null,
                        supportingText = {
                            state.errors.phoneNumber?.let { Text(it) }
                        },
                    )

                    OutlinedTextField(
                        value = state.draft.amountInput,
                        onValueChange = onAmountChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Montant") },
                        placeholder = { Text("Ex. 25000") },
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
                lines = listOf("Le quote générera une Idempotency-Key avant confirmation."),
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
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Continuer")
                }
            }
        }
    }
}

@Composable
private fun AgentCashInConfirmationContent(
    state: AgentCashInUiState.Confirmation,
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
                title = "Confirmer le cash-in",
                subtitle = "Vérifiez les informations avant de valider l’opération.",
            )
        }

        item {
            SummaryCard(
                lines = listOf(
                    "Client" to state.quote.phoneNumber,
                    "Montant" to formatKmf(state.quote.amount),
                    "Frais" to formatKmf(state.quote.fee),
                ),
            )
        }

        item {
            DebugPanel(
                lines = listOf("Idempotency-Key", state.quote.idempotencyKey),
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
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Confirmer le cash-in")
                }
            }
        }
    }

    if (state.isConfirmDialogVisible) {
        ConfirmModal(
            title = "Dernière vérification",
            message = "Cette opération va créditer le portefeuille du client. Assurez-vous que le numéro et le montant sont corrects.",
            confirmLabel = "Je confirme",
            dismissLabel = "Annuler",
            onConfirm = onConfirm,
            onDismiss = onDismissConfirmDialog,
        )
    }
}

@Composable
private fun AgentCashInSuccessContent(
    state: AgentCashInUiState.Success,
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
                title = "Cash-in enregistré",
                subtitle = "Le portefeuille du client a été crédité avec succès.",
            )
        }

        item {
            SuccessReceiptSheet(
                title = "Reçu de cash-in",
                lines = listOf(
                    "Référence" to state.receipt.transactionRef,
                    "Client" to state.receipt.clientPhoneNumber,
                    "Montant" to formatKmf(state.receipt.amount),
                    "Frais" to formatKmf(state.receipt.fee),
                    "Date" to formatIsoToDisplay(state.receipt.createdAt),
                ),
            )
        }

        item {
            DebugPanel(
                lines = listOf("Idempotency-Key", state.idempotencyKey),
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
                Text("Nouveau cash-in")
            }
        }
    }
}

@Composable
private fun AgentCashInFailureContent(
    state: AgentCashInUiState.Failure,
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
                title = "Cash-in non abouti",
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
                lines = listOf("Idempotency-Key", state.idempotencyKey),
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