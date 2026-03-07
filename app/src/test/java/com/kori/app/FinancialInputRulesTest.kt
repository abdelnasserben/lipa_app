package com.kori.app

import com.kori.app.core.ui.FinancialInputRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FinancialInputRulesTest {

    @Test
    fun `normalizes comoros phone to display format`() {
        assertEquals(
            "+269 345 67 89",
            FinancialInputRules.normalizeComorosPhoneInput("3456789"),
        )
    }

    @Test
    fun `converts valid comoros phone to api format`() {
        assertEquals(
            "+2693456789",
            FinancialInputRules.comorosPhoneToApi("+269 345 67 89"),
        )
    }

    @Test
    fun `rejects invalid comoros prefix`() {
        assertNull(FinancialInputRules.comorosPhoneToApi("+269 245 67 89"))
    }

    @Test
    fun `normalizes merchant code`() {
        assertEquals("M-123456", FinancialInputRules.normalizeMerchantCodeInput("m-12A3456"))
    }
}
