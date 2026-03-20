package com.kori.app.core.ui

import android.content.res.Resources
import androidx.annotation.StringRes
import com.kori.app.R
import java.text.NumberFormat
import java.util.Locale

object KmfAmountFormatters {
    private val kmfFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE)

    fun formatAmount(resources: Resources, amount: Long): String {
        return resources.getString(R.string.amount_kmf, kmfFormatter.format(amount))
    }

    fun normalizeInput(input: String): String = input.filter { it.isDigit() }

    fun formatInputForDisplay(input: String): String {
        val normalized = normalizeInput(input)
        return normalized
    }

    fun parseToLong(input: String): Long? = normalizeInput(input).toLongOrNull()

    fun validateAmount(
        resources: Resources,
        rawInput: String,
        min: Long = 1,
        max: Long? = null,
        @StringRes requiredMessageResId: Int = R.string.validation_amount_required,
        @StringRes invalidMessageResId: Int = R.string.validation_amount_invalid,
    ): String? {
        if (rawInput.isBlank()) return resources.getString(requiredMessageResId)

        val amount = parseToLong(rawInput) ?: return resources.getString(invalidMessageResId)
        if (amount < min) return resources.getString(R.string.validation_amount_min, formatAmount(resources, min))
        if (max != null && amount > max) return resources.getString(R.string.validation_amount_max, formatAmount(resources, max))

        return null
    }
}
