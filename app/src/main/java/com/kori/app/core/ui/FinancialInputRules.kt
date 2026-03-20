package com.kori.app.core.ui

import android.content.res.Resources
import androidx.annotation.StringRes
import com.kori.app.R

object FinancialInputRules {
    private const val COMOROS_COUNTRY_CODE = "269"
    private const val COMOROS_PREFIX = "+$COMOROS_COUNTRY_CODE"
    private const val LOCAL_PHONE_LENGTH = 7
    private const val MERCHANT_PREFIX = "M-"
    private const val MERCHANT_DIGIT_LENGTH = 6

    fun normalizeComorosPhoneInput(raw: String): String {
        val digits = raw.filter(Char::isDigit)
        val withoutCountryCode = if (digits.startsWith(COMOROS_COUNTRY_CODE)) {
            digits.drop(COMOROS_COUNTRY_CODE.length)
        } else {
            digits
        }
        val localDigits = withoutCountryCode.take(LOCAL_PHONE_LENGTH)
        return formatComorosPhoneFromLocalDigits(localDigits)
    }

    fun formatComorosPhoneForDisplay(raw: String): String = normalizeComorosPhoneInput(raw)

    fun comorosPhoneToApi(raw: String): String? {
        val local = extractLocalComorosDigits(raw)
        if (local.length != LOCAL_PHONE_LENGTH) return null
        if (local.firstOrNull() !in setOf('3', '4')) return null
        return "$COMOROS_PREFIX$local"
    }

    fun validateComorosPhone(
        raw: String,
        resources: Resources,
        @StringRes fieldLabelResId: Int,
    ): String? {
        val local = extractLocalComorosDigits(raw)
        return when {
            local.isEmpty() -> resources.getString(
                R.string.validation_required_field,
                resources.getString(fieldLabelResId),
            )
            local.length < LOCAL_PHONE_LENGTH -> resources.getString(R.string.validation_phone_incomplete)
            local.firstOrNull() !in setOf('3', '4') -> resources.getString(R.string.validation_phone_start)
            else -> null
        }
    }

    fun normalizeMerchantCodeInput(raw: String): String {
        val digits = raw.filter(Char::isDigit).take(MERCHANT_DIGIT_LENGTH)
        return "$MERCHANT_PREFIX$digits"
    }

    fun validateMerchantCode(
        raw: String,
        resources: Resources,
        @StringRes fieldLabelResId: Int = R.string.validation_field_merchant_code,
    ): String? {
        val normalized = normalizeMerchantCodeInput(raw)
        val digits = normalized.filter(Char::isDigit)
        return when {
            digits.isEmpty() -> resources.getString(
                R.string.validation_required_field,
                resources.getString(fieldLabelResId),
            )
            digits.length < MERCHANT_DIGIT_LENGTH -> resources.getString(R.string.validation_merchant_code_format)
            else -> null
        }
    }

    fun merchantCodeToApi(raw: String): String? {
        val normalized = normalizeMerchantCodeInput(raw)
        val digits = normalized.filter(Char::isDigit)
        if (digits.length != MERCHANT_DIGIT_LENGTH) return null
        return normalized
    }

    private fun extractLocalComorosDigits(raw: String): String {
        val digits = raw.filter(Char::isDigit)
        val withoutCountryCode = if (digits.startsWith(COMOROS_COUNTRY_CODE)) {
            digits.drop(COMOROS_COUNTRY_CODE.length)
        } else {
            digits
        }
        return withoutCountryCode.take(LOCAL_PHONE_LENGTH)
    }

    private fun formatComorosPhoneFromLocalDigits(localDigits: String): String {
        if (localDigits.isEmpty()) return "$COMOROS_PREFIX "

        val firstBlock = localDigits.take(3)
        val secondBlock = localDigits.drop(3).take(2)
        val thirdBlock = localDigits.drop(5).take(2)

        return buildString {
            append(COMOROS_PREFIX)
            append(' ')
            append(firstBlock)
            if (secondBlock.isNotEmpty()) {
                append(' ')
                append(secondBlock)
            }
            if (thirdBlock.isNotEmpty()) {
                append(' ')
                append(thirdBlock)
            }
        }
    }
}
