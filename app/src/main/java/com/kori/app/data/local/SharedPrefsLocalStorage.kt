package com.kori.app.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.auth.AuthSession

class SharedPrefsLocalStorage(
    context: Context,
) : LocalStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun getActiveRole(): UserRole? {
        val roleValue = sharedPreferences.getString(KEY_ACTIVE_ROLE, null) ?: return null
        return UserRole.entries.firstOrNull { it.name == roleValue }
    }

    override fun setActiveRole(role: UserRole?) {
        sharedPreferences.edit().apply {
            if (role == null) {
                remove(KEY_ACTIVE_ROLE)
            } else {
                putString(KEY_ACTIVE_ROLE, role.name)
            }
        }.apply()
    }

    override fun getLanguageCode(): String {
        return sharedPreferences.getString(KEY_LANGUAGE_CODE, DEFAULT_LANGUAGE_CODE)
            ?: DEFAULT_LANGUAGE_CODE
    }

    override fun setLanguageCode(languageCode: String) {
        sharedPreferences.edit()
            .putString(KEY_LANGUAGE_CODE, languageCode)
            .apply()
    }

    override fun isNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS_ENABLED)
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()
    }

    override fun getAuthSession(): AuthSession? {
        val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val expiresAtIso = sharedPreferences.getString(KEY_EXPIRES_AT_ISO, null) ?: return null
        val subject = sharedPreferences.getString(KEY_SUBJECT, null) ?: return null
        val issuer = sharedPreferences.getString(KEY_ISSUER, null) ?: return null

        return AuthSession(
            accessToken = accessToken,
            expiresAtIso = expiresAtIso,
            subject = subject,
            issuer = issuer,
        )
    }

    override fun setAuthSession(session: AuthSession?) {
        sharedPreferences.edit().apply {
            if (session == null) {
                remove(KEY_ACCESS_TOKEN)
                remove(KEY_REFRESH_TOKEN)
                remove(KEY_EXPIRES_AT_ISO)
                remove(KEY_SUBJECT)
                remove(KEY_ISSUER)
            } else {
                putString(KEY_ACCESS_TOKEN, session.accessToken)
                remove(KEY_REFRESH_TOKEN)
                putString(KEY_EXPIRES_AT_ISO, session.expiresAtIso)
                putString(KEY_SUBJECT, session.subject)
                putString(KEY_ISSUER, session.issuer)
            }
        }.apply()
    }

    override fun getOidcAuthStateJson(): String? = sharedPreferences.getString(KEY_OIDC_AUTH_STATE_JSON, null)

    override fun setOidcAuthStateJson(value: String?) {
        sharedPreferences.edit().apply {
            if (value == null) {
                remove(KEY_OIDC_AUTH_STATE_JSON)
            } else {
                putString(KEY_OIDC_AUTH_STATE_JSON, value)
            }
        }.apply()
    }

    private companion object {
        const val PREFS_NAME = "kori_local_storage"

        const val KEY_ACTIVE_ROLE = "active_role"
        const val KEY_LANGUAGE_CODE = "language_code"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

        const val KEY_ACCESS_TOKEN = "auth_access_token"
        const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        const val KEY_EXPIRES_AT_ISO = "auth_expires_at_iso"
        const val KEY_SUBJECT = "auth_subject"
        const val KEY_ISSUER = "auth_issuer"
        const val KEY_OIDC_AUTH_STATE_JSON = "oidc_auth_state_json"

        const val DEFAULT_LANGUAGE_CODE = "FR"
        const val DEFAULT_NOTIFICATIONS_ENABLED = true
    }
}
