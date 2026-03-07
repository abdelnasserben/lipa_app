package com.kori.app.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.formatter.MoneyInputFormatter
import java.math.BigDecimal

data class FinancialFormState(
    val amountInput: String = "",
    val amountError: String? = null,
    val isLoading: Boolean = false,
    val enabled: Boolean = true,
)

@Composable
fun FinancialForm(
    title: String,
    actionLabel: String,
    state: FinancialFormState,
    modifier: Modifier = Modifier,
    amountLabel: String = "Montant",
    amountPlaceholder: String = "0 KMF",
    helperText: String? = "Montant en francs comoriens",
    extraFields: @Composable (() -> Unit)? = null,
    onAmountChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val formattedAmount = MoneyInputFormatter.formatKmf(state.amountInput)

    SectionCard(
        title = title,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.amountInput,
                onValueChange = { onAmountChange(MoneyInputFormatter.digitsOnly(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(amountLabel) },
                placeholder = { Text(amountPlaceholder) },
                isError = state.amountError != null,
                supportingText = {
                    Text(state.amountError ?: helperText.orEmpty())
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            extraFields?.invoke()

            Button(
                onClick = onSubmit,
                enabled = state.enabled &&
                    !state.isLoading &&
                    state.amountError == null &&
                    state.amountInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}

fun validateKmfAmount(
    raw: String,
    min: BigDecimal = BigDecimal.ONE,
    max: BigDecimal? = null,
): String? {
    if (raw.isBlank()) return "Le montant est requis"

    val normalized = raw.filter { it.isDigit() }
    if (normalized.isBlank()) return "Montant invalide"

    val amount = normalized.toBigDecimalOrNull() ?: return "Montant invalide"

    if (amount < min) {
        return "Le montant minimum est ${min.toPlainString()} KMF"
    }
    if (max != null && amount > max) {
        return "Le montant maximum est ${max.toPlainString()} KMF"
    }

    return null
}