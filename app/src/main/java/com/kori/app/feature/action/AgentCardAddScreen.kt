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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriPrimary
import com.kori.app.core.designsystem.component.DebugPanel
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
            item {
                ScreenHeader(
                    title = "Ajouter une carte",
                    subtitle = "Ajoutez une carte à un client déjà existant.",
                )
            }

            item {
                SectionCard(title = "Informations") {
                    OutlinedTextField(
                        value = uiState.draft.phoneNumber,
                        onValueChange = onPhoneChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Numéro client") },
                        placeholder = { Text("+269 3XX XX XX") },
                        singleLine = true,
                        isError = uiState.errors.phoneNumber != null,
                        supportingText = { uiState.errors.phoneNumber?.let { Text(it) } },
                    )

                    OutlinedTextField(
                        value = uiState.draft.cardUid,
                        onValueChange = onCardUidChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("UID carte") },
                        singleLine = true,
                        isError = uiState.errors.cardUid != null,
                        supportingText = { uiState.errors.cardUid?.let { Text(it) } },
                    )

                    OutlinedTextField(
                        value = uiState.draft.pin,
                        onValueChange = onPinChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = uiState.errors.pin != null,
                        supportingText = { uiState.errors.pin?.let { Text(it) } },
                    )
                }
            }

            item {
                DebugPanel(lines = listOf("API cible: POST /api/v1/cards/add"))
            }

            item {
                Button(
                    onClick = onSubmit,
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary),
                ) {
                    if (uiState.isSubmitting) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Valider")
                }
            }
        }

        is AgentCardAddUiState.Success -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ScreenHeader(title = "Carte ajoutée", subtitle = "La carte a été ajoutée au client.") }
            item {
                SuccessReceiptSheet(
                    title = "Reçu d’ajout",
                    lines = listOf(
                        "Transaction" to uiState.receipt.transactionId,
                        "Client" to uiState.receipt.clientId,
                        "Carte" to uiState.receipt.cardUid,
                        "Prix carte" to formatKmf(uiState.receipt.cardPrice),
                        "Commission" to formatKmf(uiState.receipt.agentCommission),
                    ),
                )
            }
            item {
                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary),
                ) { Text("Nouvel ajout") }
            }
        }

        is AgentCardAddUiState.Failure -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 92.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ScreenHeader(title = "Échec ajout", subtitle = "La carte n’a pas pu être ajoutée.") }
            item {
                SectionCard(title = "Détails") {
                    DetailRow(label = "Code", value = uiState.code)
                    DetailRow(label = "Message", value = uiState.userMessage, showDivider = false)
                }
            }
            item {
                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KoriAccent, contentColor = KoriPrimary),
                ) { Text("Réessayer") }
            }
        }
    }
}
