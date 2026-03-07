package com.kori.app.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import com.kori.app.core.ui.KmfAmountFormatters

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
    subtitle: String? = null,
    amountLabel: String = "Montant",
    amountPlaceholder: String = "0 KMF",
    helperText: String? = "Montant en francs comoriens",
    extraFields: @Composable (() -> Unit)? = null,
    onAmountChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    SectionCard(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.amountInput,
                onValueChange = { onAmountChange(KmfAmountFormatters.normalizeInput(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(amountLabel) },
                placeholder = { Text(amountPlaceholder) },
                isError = state.amountError != null,
                supportingText = {
                    val amountHint = KmfAmountFormatters.formatInputForDisplay(state.amountInput)
                    Text(state.amountError ?: amountHint.ifBlank { helperText.orEmpty() })
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            )

            extraFields?.invoke()

            Button(
                onClick = onSubmit,
                enabled = state.enabled &&
                    !state.isLoading &&
                    state.amountError == null &&
                    state.amountInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}
