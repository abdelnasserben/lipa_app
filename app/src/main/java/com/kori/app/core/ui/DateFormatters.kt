package com.kori.app.core.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    .withLocale(Locale.FRENCH)

fun formatIsoDateForInput(iso: String): String {
    if (iso.isBlank()) return ""

    return runCatching {
        val instant = Instant.parse(iso)
        instant.atZone(ZoneOffset.UTC).toLocalDate().format(displayDateFormatter)
    }.getOrDefault("")
}

fun localDateToUtcStartOfDayIso(localDate: LocalDate): String {
    return localDate
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toString()
}

fun localDateToUtcEndOfDayIso(localDate: LocalDate): String {
    return localDate
        .atTime(23, 59, 59)
        .toInstant(ZoneOffset.UTC)
        .toString()
}

fun epochMillisToLocalDateUtc(epochMillis: Long): LocalDate {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}

fun isoToEpochMillisUtcStartOfDay(iso: String): Long? {
    if (iso.isBlank()) return null

    return runCatching {
        val localDate = Instant.parse(iso)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        localDate
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }.getOrNull()
}