package com.kori.app.data.oidc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kori.app.core.model.auth.AuthSession
import com.kori.app.core.model.auth.AuthState
import com.kori.app.data.datasource.AuthDataSource
import com.kori.app.data.local.LocalStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthState as AppAuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.EndSessionRequest
import org.json.JSONObject
import java.time.Instant
import kotlin.coroutines.resume

class OidcAuthDataSource(
    private val context: Context,
    private val localStorage: LocalStorage,
    private val oidcConfig: OidcConfig,
) : AuthDataSource {

    private val authService = AuthorizationService(context)
    private val appAuthState = AppAuthState()

    private val _authState = MutableStateFlow<AuthState>(
        localStorage.getAuthSession()?.let { AuthState.Authenticated(it) } ?: AuthState.Unauthenticated,
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun beginAuthentication(activity: Activity) {
        _authState.value = AuthState.Authenticating

        AuthorizationServiceConfiguration.fetchFromIssuer(oidcConfig.issuer) { configuration, ex ->
            if (configuration == null) {
                _authState.value = AuthState.Error(ex?.errorDescription ?: "Impossible de charger la configuration OIDC")
                return@fetchFromIssuer
            }

            val request = AuthorizationRequest.Builder(
                configuration,
                oidcConfig.clientId,
                net.openid.appauth.ResponseTypeValues.CODE,
                oidcConfig.redirectUri,
            )
                .setScope(oidcConfig.scopes)
                .build()

            val callbackIntent = Intent(activity, activity::class.java).apply {
                action = ACTION_OIDC_AUTH_COMPLETE
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val completeIntent = PendingIntent.getActivity(
                activity,
                AUTH_REQUEST_CODE,
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )

            authService.performAuthorizationRequest(request, completeIntent)
        }
    }

    override suspend fun completeAuthenticationFromIntent(intent: Intent) {
        if (intent.action != ACTION_OIDC_AUTH_COMPLETE) return

        try {
            val response = AuthorizationResponse.fromIntent(intent)
            val authException = AuthorizationException.fromIntent(intent)

            if (response == null && authException == null) {
                _authState.value = AuthState.Error(
                    "Réponse OIDC invalide ou vide. Relancez la connexion si le problème persiste.",
                )
                return
            }

            appAuthState.update(response, authException)

            if (authException != null) {
                _authState.value = AuthState.Error(authException.errorDescription ?: "Connexion OIDC interrompue")
                return
            }

            val authResponse = response ?: run {
                _authState.value = AuthState.Error("Réponse d'autorisation manquante")
                return
            }

            val tokenResponse = suspendCancellableCoroutine<Pair<net.openid.appauth.TokenResponse?, AuthorizationException?>> { cont ->
                authService.performTokenRequest(authResponse.createTokenExchangeRequest()) { resp, ex ->
                    cont.resume(resp to ex)
                }
            }

            appAuthState.update(tokenResponse.first, tokenResponse.second)

            if (tokenResponse.second != null || tokenResponse.first == null) {
                _authState.value = AuthState.Error(
                    tokenResponse.second?.errorDescription ?: "Échange de token impossible",
                )
                return
            }

            persistSession(tokenResponse.first!!)
        } catch (error: Exception) {
            _authState.value = AuthState.Error(
                error.message ?: "Impossible de finaliser la session sécurisée",
            )
        }
    }

    override suspend fun refreshSessionIfNeeded() {
        val currentState = _authState.value
        if (currentState !is AuthState.Authenticated) return

        val expiresAt = runCatching { Instant.parse(currentState.session.expiresAtIso) }.getOrNull() ?: return
        if (expiresAt.isAfter(Instant.now().plusSeconds(60))) return

        val refreshToken = appAuthState.refreshToken ?: currentState.session.refreshToken
        if (refreshToken.isBlank()) {
            logout()
            return
        }

        val config = appAuthState.authorizationServiceConfiguration
        if (config == null) {
            logout()
            return
        }

        val refreshRequest = net.openid.appauth.TokenRequest.Builder(config, oidcConfig.clientId)
            .setGrantType("refresh_token")
            .setRefreshToken(refreshToken)
            .build()

        val refreshed = suspendCancellableCoroutine<Pair<net.openid.appauth.TokenResponse?, AuthorizationException?>> { cont ->
            authService.performTokenRequest(refreshRequest) { resp, ex ->
                cont.resume(resp to ex)
            }
        }

        appAuthState.update(refreshed.first, refreshed.second)

        if (refreshed.second != null || refreshed.first == null) {
            logout()
            return
        }

        persistSession(refreshed.first!!)
    }

    override fun logout() {
        val current = appAuthState.authorizationServiceConfiguration
        if (current != null && appAuthState.idToken != null) {
            val endSessionRequest = EndSessionRequest.Builder(current)
                .setIdTokenHint(appAuthState.idToken)
                .setPostLogoutRedirectUri(oidcConfig.postLogoutRedirectUri)
                .build()
            context.startActivity(
                authService.getEndSessionRequestIntent(endSessionRequest).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }

        localStorage.setAuthSession(null)
        _authState.value = AuthState.Unauthenticated
    }

    private fun persistSession(tokenResponse: net.openid.appauth.TokenResponse) {
        val idToken = tokenResponse.idToken.orEmpty()
        val claims = runCatching { JSONObject(decodeJwtPayload(idToken)) }.getOrNull()

        val subject = claims?.optString("sub")?.takeIf { it.isNotBlank() } ?: "unknown"
        val issuer = claims?.optString("iss")?.takeIf { it.isNotBlank() } ?: oidcConfig.issuer.toString()

        val expiresAt = tokenResponse.accessTokenExpirationTime?.let { Instant.ofEpochMilli(it) }
            ?: Instant.now().plusSeconds(300)

        val session = AuthSession(
            accessToken = tokenResponse.accessToken.orEmpty(),
            refreshToken = tokenResponse.refreshToken.orEmpty(),
            expiresAtIso = expiresAt.toString(),
            subject = subject,
            issuer = issuer,
        )

        localStorage.setAuthSession(session)
        _authState.value = AuthState.Authenticated(session)
    }

    private fun decodeJwtPayload(jwt: String): String {
        val chunks = jwt.split('.')
        if (chunks.size < 2) return "{}"
        return String(android.util.Base64.decode(chunks[1], android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP))
    }

    companion object {
        const val ACTION_OIDC_AUTH_COMPLETE = "com.kori.app.oidc.AUTH_COMPLETE"
        private const val AUTH_REQUEST_CODE = 9001
    }
}
