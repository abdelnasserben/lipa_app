package com.kori.app.core.ui

import java.text.NumberFormat
import java.util.Locale

object KmfAmountFormatters {
    private val kmfFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE)

    fun formatAmount(amount: Long): String = "${kmfFormatter.format(amount)} KMF"

    fun normalizeInput(input: String): String = input.filter { it.isDigit() }

    fun formatInputForDisplay(input: String): String {
        val normalized = normalizeInput(input)
        val amount = normalized.toLongOrNull() ?: return ""
        return formatAmount(amount)
    }

    fun parseToLong(input: String): Long? = normalizeInput(input).toLongOrNull()

    fun validateAmount(
        rawInput: String,
        min: Long = 1,
        max: Long? = null,
        requiredMessage: String = "Saisissez un montant.",
        invalidMessage: String = "Montant invalide.",
    ): String? {
        if (rawInput.isBlank()) return requiredMessage

        val amount = parseToLong(rawInput) ?: return invalidMessage
        if (amount < min) return "Le montant minimum autorisé est ${formatAmount(min)}."
        if (max != null && amount > max) return "Le montant maximum autorisé est ${formatAmount(max)}."

        return null
    }
}
