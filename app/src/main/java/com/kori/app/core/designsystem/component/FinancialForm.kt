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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kori.app.R
import com.kori.app.core.ui.KmfAmountFormatters
import com.kori.app.core.ui.formatKmf

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
    amountLabel: String? = null,
    amountPlaceholder: String? = null,
    helperText: String? = null,
    extraFields: @Composable (() -> Unit)? = null,
    onAmountChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val resolvedAmountLabel = amountLabel ?: stringResource(R.string.common_amount)
    val resolvedAmountPlaceholder = amountPlaceholder ?: stringResource(R.string.financial_form_amount_placeholder)
    val resolvedHelperText = helperText ?: stringResource(R.string.financial_form_amount_helper)

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
                label = { Text(resolvedAmountLabel) },
                placeholder = { Text(resolvedAmountPlaceholder) },
                isError = state.amountError != null,
                supportingText = {
                    val resources = LocalContext.current.resources
                    val parsedAmount = KmfAmountFormatters.parseToLong(state.amountInput)
                    val amountHint = if (parsedAmount != null) formatKmf(resources, parsedAmount) else ""
                    Text(state.amountError ?: amountHint.ifBlank { resolvedHelperText })
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
