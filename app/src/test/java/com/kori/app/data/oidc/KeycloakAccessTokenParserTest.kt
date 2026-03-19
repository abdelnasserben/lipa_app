package com.kori.app.data.oidc

import com.kori.app.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class KeycloakAccessTokenParserTest {

    @Test
    fun `parse returns supported app role and actorRef from kori-api resource access`() {
        val token = jwtFor(
            aud = listOf("account", "kori-api"),
            actorRef = "CLI-0001",
            roles = listOf("CLIENT"),
        )

        val result = KeycloakAccessTokenParser.parse(token)

        assertTrue(result is KeycloakAccessTokenParseResult.Success)
        val parsed = (result as KeycloakAccessTokenParseResult.Success).token
        assertEquals(UserRole.CLIENT, parsed.userRole)
        assertEquals("CLI-0001", parsed.actorRef)
        assertEquals("user-123", parsed.subject)
        assertEquals("https://auth.example/realms/kori", parsed.issuer)
    }

    @Test
    fun `parse rejects token when kori-api audience is missing`() {
        val token = jwtFor(
            aud = listOf("account"),
            actorRef = "AGT-0001",
            roles = listOf("AGENT"),
        )

        val result = KeycloakAccessTokenParser.parse(token)

        assertTrue(result is KeycloakAccessTokenParseResult.Failure)
        assertEquals(
            "Token Keycloak invalide : audience kori-api absente.",
            (result as KeycloakAccessTokenParseResult.Failure).message,
        )
    }

    @Test
    fun `parse rejects unsupported or ambiguous roles`() {
        val unsupported = jwtFor(
            aud = listOf("kori-api"),
            actorRef = "ADM-0001",
            roles = listOf("ADMIN"),
        )
        val ambiguous = jwtFor(
            aud = listOf("kori-api"),
            actorRef = "MIX-0001",
            roles = listOf("CLIENT", "AGENT"),
        )

        val unsupportedResult = KeycloakAccessTokenParser.parse(unsupported)
        val ambiguousResult = KeycloakAccessTokenParser.parse(ambiguous)

        assertTrue(unsupportedResult is KeycloakAccessTokenParseResult.Failure)
        assertTrue(ambiguousResult is KeycloakAccessTokenParseResult.Failure)
    }

    @Test
    fun `parse rejects token without actorRef`() {
        val token = jwtFor(
            aud = listOf("kori-api"),
            actorRef = null,
            roles = listOf("MERCHANT"),
        )

        val result = KeycloakAccessTokenParser.parse(token)

        assertTrue(result is KeycloakAccessTokenParseResult.Failure)
        assertEquals(
            "Token Keycloak invalide : actorRef absent.",
            (result as KeycloakAccessTokenParseResult.Failure).message,
        )
    }

    private fun jwtFor(
        aud: List<String>,
        actorRef: String?,
        roles: List<String>,
    ): String {
        val header = """{"alg":"none","typ":"JWT"}"""
        val audJson = aud.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        val actorRefJson = actorRef?.let { ",\"actorRef\":\"$it\"" }.orEmpty()
        val rolesJson = roles.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        val payload = """{"sub":"user-123","iss":"https://auth.example/realms/kori","aud":$audJson$actorRefJson,"resource_access":{"kori-api":{"roles":$rolesJson}}}"""
        return "${encode(header)}.${encode(payload)}.signature"
    }

    private fun encode(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))
}
