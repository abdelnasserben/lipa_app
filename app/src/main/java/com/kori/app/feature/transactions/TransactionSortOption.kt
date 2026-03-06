package com.kori.app.feature.transactions

enum class TransactionSortOption(
    val label: String,
    val backendValue: String,
) {
    DATE_DESC(
        label = "Plus récentes",
        backendValue = "-createdAt",
    ),
    DATE_ASC(
        label = "Plus anciennes",
        backendValue = "createdAt",
    ),
}