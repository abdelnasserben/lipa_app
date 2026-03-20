package com.kori.app.feature.transactions

import androidx.annotation.StringRes
import com.kori.app.R

enum class TransactionSortOption(
    @StringRes val labelResId: Int,
    val backendValue: String,
) {
    DATE_DESC(
        labelResId = R.string.transactions_sort_recent,
        backendValue = "-createdAt",
    ),
    DATE_ASC(
        labelResId = R.string.transactions_sort_oldest,
        backendValue = "createdAt",
    ),
}
