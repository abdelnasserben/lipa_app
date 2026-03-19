package com.kori.app.data.oidc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import com.kori.app.MainActivity
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
import net.openid.appauth.EndSessionResponse
import org.json.JSONObject
import java.time.Instant
import kotlin.coroutines.resume

class OidcAuthDataSource(
    private val context: Context,
    private val localStorage: LocalStorage,
    private val oidcConfig: OidcConfig,
) : AuthDataSource {

    private val authService = AuthorizationService(context)
    private var appAuthState = AppAuthState()
    private var cachedServiceConfiguration: AuthorizationServiceConfiguration? = null
    private var authFlowInProgress = false
    private var logoutFlowInProgress = false

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        restorePersistedState()
    }

    override fun startLogin(activity: Activity) {
        if (authFlowInProgress) {
            Log.w(TAG, "startLogin ignored: authorization flow already in progress")
            return
        }

        authFlowInProgress = true
        logoutFlowInProgress = false
        _authState.value = AuthState.Authenticating
        Log.i(TAG, "Starting login with issuer=${oidcConfig.issuer}, redirectUri=${oidcConfig.redirectUri}")

        activity.runOnUiThread {
            fetchConfiguration { configuration, exception ->
                if (configuration == null) {
                    authFlowInProgress = false
                    val message = exception?.errorDescription ?: "Impossible de charger la configuration OIDC"
                    Log.e(TAG, "Discovery failed before login: $message", exception)
                    _authState.value = AuthState.Error(message)
                    return@fetchConfiguration
                }

                val request = AuthorizationRequest.Builder(
                    configuration,
                    oidcConfig.clientId,
                    net.openid.appauth.ResponseTypeValues.CODE,
                    oidcConfig.redirectUri,
                )
                    .setScope(oidcConfig.scopes)
                    .build()

                Log.d(
                    TAG,
                    "AuthorizationRequest ready: scopes=${oidcConfig.scopes}, codeVerifierLength=${request.codeVerifier?.length}",
                )

                authService.performAuthorizationRequest(
                    request,
                    createCompletionPendingIntent(activity, ACTION_OIDC_AUTH_COMPLETE, AUTH_REQUEST_CODE),
                    createCompletionPendingIntent(activity, ACTION_OIDC_AUTH_CANCELED, AUTH_CANCEL_REQUEST_CODE),
                )
            }
        }
    }

    override suspend fun handleAuthorizationResponse(intent: Intent) {
        Log.i(TAG, "Received callback intent: action=${intent.action}, data=${intent.data}")

        when {
            isLogoutIntent(intent) -> handleLogoutResponse(intent)
            isAuthIntent(intent) -> handleAuthResponse(intent)
            else -> Log.d(TAG, "Ignoring non-OIDC intent callback")
        }
    }

    override suspend fun ensureFreshAccessToken(): String? {
        if (!isAuthenticated()) {
            Log.w(TAG, "ensureFreshAccessToken called without an authenticated session")
            return null
        }

        return suspendCancellableCoroutine { cont ->
            appAuthState.performActionWithFreshTokens(authService) { accessToken, _, exception ->
                if (exception != null) {
                    Log.e(TAG, "Token refresh failed during performActionWithFreshTokens", exception)
                    clearSession()
                    _authState.value = AuthState.Error(
                        exception.errorDescription ?: "Impossible de rafraîchir la session OIDC",
                    )
                    cont.resume(null)
                    return@performActionWithFreshTokens
                }

                Log.i(TAG, "performActionWithFreshTokens completed successfully")
                persistCurrentState()
                cont.resume(accessToken)
            }
        }
    }

    override fun isAuthenticated(): Boolean {
        val authenticated = appAuthState.isAuthorized && _authState.value is AuthState.Authenticated
        Log.d(TAG, "isAuthenticated() -> $authenticated")
        return authenticated
    }

    override fun logout(activity: Activity) {
        Log.i(TAG, "Starting logout")
        logoutFlowInProgress = true
        authFlowInProgress = false

        val configuration = appAuthState.authorizationServiceConfiguration ?: cachedServiceConfiguration
        val idTokenHint = appAuthState.idToken
        val endSessionEndpoint = configuration?.endSessionEndpoint

        if (configuration == null || idTokenHint.isNullOrBlank() || endSessionEndpoint == null) {
            Log.w(
                TAG,
                "Logout fallback to local clearSession: config=${configuration != null}, idToken=${!idTokenHint.isNullOrBlank()}, endSession=${endSessionEndpoint != null}",
            )
            clearSession()
            logoutFlowInProgress = false
            return
        }

        val request = EndSessionRequest.Builder(configuration)
            .setIdTokenHint(idTokenHint)
            .setPostLogoutRedirectUri(oidcConfig.postLogoutRedirectUri)
            .build()

        authService.performEndSessionRequest(
            request,
            createCompletionPendingIntent(activity, ACTION_OIDC_LOGOUT_COMPLETE, LOGOUT_REQUEST_CODE),
            createCompletionPendingIntent(activity, ACTION_OIDC_LOGOUT_CANCELED, LOGOUT_CANCEL_REQUEST_CODE),
        )
    }

    override fun clearSession() {
        Log.i(TAG, "clearSession(): clearing persisted AuthState and session snapshot")
        appAuthState = AppAuthState()
        cachedServiceConfiguration = null
        authFlowInProgress = false
        logoutFlowInProgress = false
        localStorage.setOidcAuthStateJson(null)
        localStorage.setAuthSession(null)
        _authState.value = AuthState.Unauthenticated
        Log.i(TAG, "Logout/clearSession completed")
    }

    private suspend fun handleAuthResponse(intent: Intent) {
        authFlowInProgress = false

        if (intent.action == ACTION_OIDC_AUTH_CANCELED) {
            val exception = AuthorizationException.fromIntent(intent)
            Log.w(TAG, "Authorization flow canceled: ${exception?.errorDescription}", exception)
            _authState.value = AuthState.Error(
                exception?.errorDescription ?: "Connexion OIDC annulée par l'utilisateur",
            )
            return
        }

        val response = AuthorizationResponse.fromIntent(intent)
        val authException = AuthorizationException.fromIntent(intent)
        Log.d(TAG, "AuthorizationResponse.fromIntent(intent) -> ${response != null}")
        Log.d(TAG, "AuthorizationException.fromIntent(intent) -> ${authException != null}")

        if (response == null && authException == null) {
            val message = "Intent de callback OIDC reçu sans réponse ni erreur"
            Log.e(TAG, message)
            _authState.value = AuthState.Error(message)
            return
        }

        appAuthState.update(response, authException)
        cachedServiceConfiguration = response?.request?.configuration ?: appAuthState.authorizationServiceConfiguration

        if (authException != null) {
            Log.e(TAG, "Authorization callback contains an error", authException)
            _authState.value = AuthState.Error(
                authException.errorDescription ?: "Connexion OIDC interrompue",
            )
            return
        }

        val authResponse = response ?: run {
            val message = "Réponse d'autorisation manquante dans le callback OIDC"
            Log.e(TAG, message)
            _authState.value = AuthState.Error(message)
            return
        }

        _authState.value = AuthState.Authenticating
        Log.i(TAG, "Beginning token exchange")
        val (tokenResponse, tokenException) = suspendCancellableCoroutine<Pair<net.openid.appauth.TokenResponse?, AuthorizationException?>> { cont ->
            authService.performTokenRequest(authResponse.createTokenExchangeRequest()) { resp, ex ->
                cont.resume(resp to ex)
            }
        }

        appAuthState.update(tokenResponse, tokenException)

        if (tokenException != null || tokenResponse == null) {
            val message = tokenException?.errorDescription ?: "Échange code -> tokens impossible"
            Log.e(TAG, "Token exchange failed: $message", tokenException)
            _authState.value = AuthState.Error(message)
            return
        }

        Log.i(TAG, "Token exchange succeeded")
        persistCurrentState()
    }

    private fun handleLogoutResponse(intent: Intent) {
        logoutFlowInProgress = false
        val response = EndSessionResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)
        Log.d(TAG, "Logout callback response received: hasResponse=${response != null}, hasException=${exception != null}")
        if (exception != null) {
            Log.w(TAG, "Logout callback contains an error; clearing local session anyway", exception)
        }
        clearSession()
    }

    private fun fetchConfiguration(
        callback: (AuthorizationServiceConfiguration?, AuthorizationException?) -> Unit,
    ) {
        cachedServiceConfiguration?.let {
            callback(it, null)
            return
        }

        Log.i(TAG, "Fetching discovery document from ${oidcConfig.discoveryUri}")
        AuthorizationServiceConfiguration.fetchFromUrl(oidcConfig.discoveryUri) { configuration, exception ->
            if (configuration != null) {
                cachedServiceConfiguration = configuration
                Log.i(
                    TAG,
                    "Discovery succeeded: auth=${configuration.authorizationEndpoint}, token=${configuration.tokenEndpoint}, endSession=${configuration.endSessionEndpoint}",
                )
            } else {
                Log.e(TAG, "Discovery failed", exception)
            }
            callback(configuration, exception)
        }
    }

    private fun restorePersistedState() {
        val persistedJson = localStorage.getOidcAuthStateJson()
        Log.i(TAG, "Restoring AuthState from storage: found=${persistedJson != null}")

        if (persistedJson.isNullOrBlank()) {
            _authState.value = AuthState.Unauthenticated
            Log.d(TAG, "No persisted AppAuth state found")
            return
        }

        val restoredState = runCatching { AppAuthState.jsonDeserialize(persistedJson) }
            .onFailure {
                Log.e(TAG, "Failed to deserialize persisted AppAuth state", it)
                localStorage.setOidcAuthStateJson(null)
                localStorage.setAuthSession(null)
            }
            .getOrNull()
            ?: run {
                _authState.value = AuthState.Unauthenticated
                return
            }

        appAuthState = restoredState
        cachedServiceConfiguration = restoredState.authorizationServiceConfiguration

        val session = createSessionFromStateOrNull(restoredState)
        if (restoredState.isAuthorized && session != null) {
            localStorage.setAuthSession(session)
            _authState.value = AuthState.Authenticated(session)
            Log.i(TAG, "AuthState restored successfully for subject=${session.subject}")
        } else {
            Log.w(TAG, "Persisted AuthState is not authorized anymore; clearing local session")
            clearSession()
        }
    }

    private fun persistCurrentState() {
        val session = createSessionFromStateOrNull(appAuthState)
        if (!appAuthState.isAuthorized || session == null) {
            Log.w(TAG, "Refusing to persist non-authorized AppAuth state")
            clearSession()
            return
        }

        // AppAuth AuthState remains the source of truth so browser round-trips survive process death.
        localStorage.setOidcAuthStateJson(appAuthState.jsonSerializeString())
        localStorage.setAuthSession(session)
        _authState.value = AuthState.Authenticated(session)
        Log.i(TAG, "Persisted AuthState for subject=${session.subject}")
    }

    private fun createSessionFromStateOrNull(state: AppAuthState): AuthSession? {
        val accessToken = state.accessToken ?: return null
        val refreshToken = state.refreshToken.orEmpty()
        val expiresAt = state.accessTokenExpirationTime?.let { Instant.ofEpochMilli(it) }
            ?: Instant.now().plusSeconds(300)

        val claims = state.idToken
            ?.takeIf { it.isNotBlank() }
            ?.let(::decodeJwtPayload)
            ?.let { payload -> runCatching { JSONObject(payload) }.getOrNull() }

        val subject = claims?.optString("sub")?.takeIf { it.isNotBlank() } ?: "unknown"
        val issuer = claims?.optString("iss")?.takeIf { it.isNotBlank() } ?: oidcConfig.issuer.toString()

        return AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtIso = expiresAt.toString(),
            subject = subject,
            issuer = issuer,
        )
    }

    private fun createCompletionPendingIntent(
        activity: Activity,
        action: String,
        requestCode: Int,
    ): PendingIntent {
        // We always return to MainActivity so Compose can finish the OIDC flow exactly once via OidcIntentBus.
        val callbackIntent = Intent(activity, MainActivity::class.java).apply {
            this.action = action
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        return PendingIntent.getActivity(
            activity,
            requestCode,
            callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    private fun isAuthIntent(intent: Intent): Boolean {
        return intent.action == ACTION_OIDC_AUTH_COMPLETE ||
            intent.action == ACTION_OIDC_AUTH_CANCELED ||
            intent.data?.toString()?.startsWith(oidcConfig.redirectUri.toString()) == true
    }

    private fun isLogoutIntent(intent: Intent): Boolean {
        return intent.action == ACTION_OIDC_LOGOUT_COMPLETE ||
            intent.action == ACTION_OIDC_LOGOUT_CANCELED ||
            intent.data?.toString()?.startsWith(oidcConfig.postLogoutRedirectUri.toString()) == true
    }

    private fun decodeJwtPayload(jwt: String): String {
        val chunks = jwt.split('.')
        if (chunks.size < 2) return "{}"
        return String(Base64.decode(chunks[1], Base64.URL_SAFE or Base64.NO_WRAP))
    }

    private companion object {
        const val TAG = "OIDC"
        const val ACTION_OIDC_AUTH_COMPLETE = "com.kori.app.oidc.AUTH_COMPLETE"
        const val ACTION_OIDC_AUTH_CANCELED = "com.kori.app.oidc.AUTH_CANCELED"
        const val ACTION_OIDC_LOGOUT_COMPLETE = "com.kori.app.oidc.LOGOUT_COMPLETE"
        const val ACTION_OIDC_LOGOUT_CANCELED = "com.kori.app.oidc.LOGOUT_CANCELED"
        const val AUTH_REQUEST_CODE = 9001
        const val AUTH_CANCEL_REQUEST_CODE = 9002
        const val LOGOUT_REQUEST_CODE = 9003
        const val LOGOUT_CANCEL_REQUEST_CODE = 9004
    }
}
