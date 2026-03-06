package com.kori.app.core.ui

fun maskToken(token: String): String {
    if (token.length <= 12) return "••••••••"
    return "${token.take(6)}••••••${token.takeLast(6)}"
}