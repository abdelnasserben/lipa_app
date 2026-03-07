package com.kori.app.core.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatKmf(amount: Long): String = KmfAmountFormatters.formatAmount(amount)

fun formatIsoToDisplay(iso: String): String {
    return runCatching {
        val instant = Instant.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm")
            .withLocale(Locale.FRENCH)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    }.getOrElse { iso }
}
