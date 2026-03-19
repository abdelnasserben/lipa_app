package com.kori.app.data.oidc

import android.net.Uri
import androidx.core.net.toUri
import com.kori.app.BuildConfig

data class OidcConfig(
    val issuer: Uri,
    val discoveryUri: Uri,
    val clientId: String,
    val redirectUri: Uri,
    val postLogoutRedirectUri: Uri,
    val scopes: String,
) {
    companion object {
        fun fromBuildConfig(): OidcConfig {
            val issuer = BuildConfig.OIDC_ISSUER.toUri()
            val discoveryUri = "${BuildConfig.OIDC_ISSUER}/.well-known/openid-configuration".toUri()

            return OidcConfig(
                issuer = issuer,
                discoveryUri = discoveryUri,
                clientId = BuildConfig.OIDC_CLIENT_ID,
                redirectUri = BuildConfig.OIDC_REDIRECT_URI.toUri(),
                postLogoutRedirectUri = BuildConfig.OIDC_POST_LOGOUT_REDIRECT_URI.toUri(),
                scopes = BuildConfig.OIDC_SCOPES,
            )
        }
    }
}
