package com.kori.app.core.designsystem.formatter

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object MoneyInputFormatter {
    fun digitsOnly(input: String): String = input.filter { it.isDigit() }

    fun formatKmf(input: String): String {
        val digits = digitsOnly(input)
        if (digits.isBlank()) return ""
        val value = digits.toLongOrNull() ?: return digits
        val symbols = DecimalFormatSymbols(Locale.FRANCE).apply {
            groupingSeparator = ' '
        }

        return DecimalFormat("#,###", symbols).format(value) + " KMF"
    }
}