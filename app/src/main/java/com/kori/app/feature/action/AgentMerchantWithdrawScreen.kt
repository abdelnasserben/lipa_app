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
fun AgentMerchantWithdrawScreen(
    uiState: AgentMerchantWithdrawUiState,
    onMerchantCodeChanged: (String) -> Unit,
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
        is AgentMerchantWithdrawUiState.Form -> AgentMerchantWithdrawFormContent(
            state = uiState,
            onMerchantCodeChanged = onMerchantCodeChanged,
            onAmountChanged = onAmountChanged,
            onContinue = onContinue,
            modifier = modifier,
        )

        is AgentMerchantWithdrawUiState.Confirmation -> AgentMerchantWithdrawConfirmationContent(
            state = uiState,
            onOpenConfirmDialog = onOpenConfirmDialog,
            onDismissConfirmDialog = onDismissConfirmDialog,
            onConfirm = onConfirm,
            onEdit = onEdit,
            modifier = modifier,
        )

        is AgentMerchantWithdrawUiState.Success -> AgentMerchantWithdrawSuccessContent(
            state = uiState,
            onRestart = onRestart,
            modifier = modifier,
        )

        is AgentMerchantWithdrawUiState.Failure -> AgentMerchantWithdrawFailureContent(
            state = uiState,
            onRestart = onRestart,
            modifier = modifier,
        )
    }
}

@Composable
private fun AgentMerchantWithdrawFormContent(
    state: AgentMerchantWithdrawUiState.Form,
    onMerchantCodeChanged: (String) -> Unit,
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
                title = "Retrait marchand",
                subtitle = "Saisissez le code marchand et le montant à retirer.",
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
                        value = state.draft.merchantCode,
                        onValueChange = onMerchantCodeChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Code marchand") },
                        placeholder = { Text("Ex. MER-0061") },
                        singleLine = true,
                        isError = state.errors.merchantCode != null,
                        supportingText = {
                            state.errors.merchantCode?.let { Text(it) }
                        },
                    )

                    OutlinedTextField(
                        value = state.draft.amountInput,
                        onValueChange = onAmountChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Montant") },
                        placeholder = { Text("Ex. 120000") },
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
private fun AgentMerchantWithdrawConfirmationContent(
    state: AgentMerchantWithdrawUiState.Confirmation,
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
                title = "Confirmer le retrait marchand",
                subtitle = "Vérifiez les informations avant de valider l’opération.",
            )
        }

        item {
            SummaryCard(
                lines = listOf(
                    "Marchand" to state.quote.merchantCode,
                    "Montant" to formatKmf(state.quote.amount),
                    "Frais" to formatKmf(state.quote.fee),
                    "Commission" to formatKmf(state.quote.commission),
                    "Total débité marchand" to formatKmf(state.quote.totalDebitedMerchant),
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
                    Text("Confirmer le retrait")
                }
            }
        }
    }

    if (state.isConfirmDialogVisible) {
        ConfirmModal(
            title = "Dernière vérification",
            message = "Cette opération va débiter le solde marchand et générer une commission agent. Assurez-vous que le code marchand et le montant sont corrects.",
            confirmLabel = "Je confirme",
            dismissLabel = "Annuler",
            onConfirm = onConfirm,
            onDismiss = onDismissConfirmDialog,
        )
    }
}

@Composable
private fun AgentMerchantWithdrawSuccessContent(
    state: AgentMerchantWithdrawUiState.Success,
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
                title = "Retrait marchand enregistré",
                subtitle = "L’opération a été finalisée avec succès.",
            )
        }

        item {
            SuccessReceiptSheet(
                title = "Reçu de retrait marchand",
                lines = listOf(
                    "Référence" to state.receipt.transactionRef,
                    "Marchand" to state.receipt.merchantCode,
                    "Montant" to formatKmf(state.receipt.amount),
                    "Frais" to formatKmf(state.receipt.fee),
                    "Commission" to formatKmf(state.receipt.commission),
                    "Total débité marchand" to formatKmf(state.receipt.totalDebitedMerchant),
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
                Text("Nouveau retrait")
            }
        }
    }
}

@Composable
private fun AgentMerchantWithdrawFailureContent(
    state: AgentMerchantWithdrawUiState.Failure,
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
                title = "Retrait marchand non abouti",
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