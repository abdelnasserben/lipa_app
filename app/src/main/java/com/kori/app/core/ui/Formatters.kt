package com.kori.app.core.ui

import android.content.res.Resources
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatKmf(
    resources: Resources,
    amount: Long,
): String = KmfAmountFormatters.formatAmount(resources, amount)

fun formatIsoToDisplay(
    resources: Resources,
    iso: String,
): String {
    val locale = resources.configuration.locales[0] ?: Locale.getDefault()
    return runCatching {
        val instant = Instant.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm")
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    }.getOrElse { iso }
}
