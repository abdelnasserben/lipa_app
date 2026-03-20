package com.kori.app.core.ui

import androidx.annotation.StringRes
import com.kori.app.R
import com.kori.app.core.model.activity.ActivityCategory
import com.kori.app.core.model.activity.ActivityStatus
import com.kori.app.core.model.activity.ActivityType

@StringRes
fun ActivityType.displayLabelResId(): Int = when (this) {
    ActivityType.PAYMENT -> R.string.activity_type_payment
    ActivityType.TRANSFER -> R.string.activity_type_transfer
    ActivityType.CARD -> R.string.activity_type_card
    ActivityType.COLLECTION -> R.string.activity_type_collection
    ActivityType.TERMINAL -> R.string.activity_type_terminal
    ActivityType.CASH_IN -> R.string.activity_type_cash_in
    ActivityType.MERCHANT_WITHDRAW -> R.string.activity_type_merchant_withdraw
    ActivityType.FIELD_OPERATION -> R.string.activity_type_field_operation
}

fun ActivityType.displayLabel(resources: android.content.res.Resources): String = resources.getString(displayLabelResId())

@StringRes
fun ActivityStatus.displayLabelResId(): Int = when (this) {
    ActivityStatus.PENDING -> R.string.activity_status_pending
    ActivityStatus.COMPLETED -> R.string.activity_status_completed
    ActivityStatus.FAILED -> R.string.activity_status_failed
}

fun ActivityStatus.displayLabel(resources: android.content.res.Resources): String = resources.getString(displayLabelResId())

@StringRes
fun ActivityCategory.displayLabelResId(): Int = when (this) {
    ActivityCategory.PAYMENT -> R.string.activity_category_payment
    ActivityCategory.TRANSFER -> R.string.activity_category_transfer
    ActivityCategory.CARD -> R.string.activity_category_card
    ActivityCategory.TERMINAL -> R.string.activity_category_terminal
    ActivityCategory.TERRAIN -> R.string.activity_category_field
}

fun ActivityCategory.displayLabel(resources: android.content.res.Resources): String = resources.getString(displayLabelResId())
