package com.kori.app.data.oidc

import com.kori.app.core.model.UserRole
import org.json.JSONArray
import org.json.JSONObject
import java.util.Base64

internal data class ParsedKeycloakAccessToken(
    val subject: String,
    val issuer: String,
    val userRole: UserRole,
    val actorRef: String,
)

internal sealed interface KeycloakAccessTokenParseResult {
    data class Success(
        val token: ParsedKeycloakAccessToken,
    ) : KeycloakAccessTokenParseResult

    data class Failure(
        val message: String,
    ) : KeycloakAccessTokenParseResult
}

internal object KeycloakAccessTokenParser {
    private const val REQUIRED_AUDIENCE = "kori-api"
    private const val RESOURCE_ACCESS = "resource_access"
    private const val ACTOR_REF = "actorRef"

    fun parse(accessToken: String): KeycloakAccessTokenParseResult {
        val payload = decodePayload(accessToken) ?: return KeycloakAccessTokenParseResult.Failure(
            "Token JWT Keycloak invalide : payload illisible.",
        )
        val claims = runCatching { JSONObject(payload) }.getOrElse {
            return KeycloakAccessTokenParseResult.Failure(
                "Token JWT Keycloak invalide : payload JSON illisible.",
            )
        }

        if (!containsRequiredAudience(claims)) {
            return KeycloakAccessTokenParseResult.Failure(
                "Token Keycloak invalide : audience kori-api absente.",
            )
        }

        val actorRef = claims.optString(ACTOR_REF).trim()
        if (actorRef.isEmpty()) {
            return KeycloakAccessTokenParseResult.Failure(
                "Token Keycloak invalide : actorRef absent.",
            )
        }

        val supportedRole = extractSupportedRole(claims) ?: return extractRoleFailure(claims)

        val subject = claims.optString("sub").takeIf { it.isNotBlank() } ?: "unknown"
        val issuer = claims.optString("iss").takeIf { it.isNotBlank() } ?: "unknown"

        return KeycloakAccessTokenParseResult.Success(
            ParsedKeycloakAccessToken(
                subject = subject,
                issuer = issuer,
                userRole = supportedRole,
                actorRef = actorRef,
            ),
        )
    }

    private fun decodePayload(jwt: String): String? {
        val segments = jwt.split('.')
        if (segments.size < 2) return null

        return runCatching {
            val decoded = Base64.getUrlDecoder().decode(segments[1])
            String(decoded, Charsets.UTF_8)
        }.getOrNull()
    }

    private fun containsRequiredAudience(claims: JSONObject): Boolean {
        val audienceClaim = claims.opt("aud") ?: return false
        return when (audienceClaim) {
            is String -> audienceClaim == REQUIRED_AUDIENCE
            is JSONArray -> audienceClaim.toStringList().any { it == REQUIRED_AUDIENCE }
            else -> false
        }
    }

    private fun extractSupportedRole(claims: JSONObject): UserRole? {
        val roles = extractRoles(claims)
        val supportedRoles = roles.mapNotNull(::mapSupportedRole).distinct()

        return when (supportedRoles.size) {
            1 -> supportedRoles.single()
            else -> null
        }
    }

    private fun extractRoleFailure(claims: JSONObject): KeycloakAccessTokenParseResult.Failure {
        val roles = extractRoles(claims)
        if (roles.isEmpty()) {
            return KeycloakAccessTokenParseResult.Failure(
                "Token Keycloak invalide : aucun rôle trouvé dans resource_access.kori-api.roles.",
            )
        }

        val supportedRoles = roles.mapNotNull(::mapSupportedRole).distinct()
        if (supportedRoles.size > 1) {
            return KeycloakAccessTokenParseResult.Failure(
                "Token Keycloak invalide : plusieurs rôles applicatifs supportés détectés (${supportedRoles.joinToString()}).",
            )
        }

        return KeycloakAccessTokenParseResult.Failure(
            "Token Keycloak invalide : rôle non supporté (${roles.joinToString()}).",
        )
    }

    private fun extractRoles(claims: JSONObject): List<String> {
        val resourceAccess = claims.optJSONObject(RESOURCE_ACCESS) ?: return emptyList()
        val koriApi = resourceAccess.optJSONObject(REQUIRED_AUDIENCE) ?: return emptyList()
        val rolesArray = koriApi.optJSONArray("roles") ?: return emptyList()
        return rolesArray.toStringList()
            .map(String::trim)
            .filter(String::isNotEmpty)
    }

    private fun mapSupportedRole(roleName: String): UserRole? = when (roleName.uppercase()) {
        UserRole.CLIENT.name -> UserRole.CLIENT
        UserRole.MERCHANT.name -> UserRole.MERCHANT
        UserRole.AGENT.name -> UserRole.AGENT
        else -> null
    }

    private fun JSONArray.toStringList(): List<String> = buildList(length()) {
        for (index in 0 until length()) {
            optString(index)
                .takeIf { it.isNotBlank() }
                ?.let(::add)
        }
    }
}
