package com.kori.app.data.remote

import com.kori.app.BuildConfig
import com.kori.app.core.network.NetworkHttpException
import com.kori.app.data.repository.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

class ApiHttpClient(
    private val authService: AuthService,
    private val baseUrl: String = BuildConfig.API_BASE_URL,
) {

    suspend fun get(
        path: String,
        query: Map<String, String?> = emptyMap(),
    ): JSONObject = request(
        method = "GET",
        path = path,
        query = query,
    )

    suspend fun post(
        path: String,
        body: JSONObject,
        idempotencyKey: String? = null,
    ): JSONObject = request(
        method = "POST",
        path = path,
        body = body,
        idempotencyKey = idempotencyKey,
    )

    private suspend fun request(
        method: String,
        path: String,
        query: Map<String, String?> = emptyMap(),
        body: JSONObject? = null,
        idempotencyKey: String? = null,
    ): JSONObject = withContext(Dispatchers.IO) {
        val token = authService.getValidAccessToken()
            ?: throw IOException("Authenticated API call requested without a valid access token.")

        val connection = (URL(buildUrl(path, query)).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doInput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("X-Correlation-Id", UUID.randomUUID().toString())
            if (idempotencyKey != null) {
                setRequestProperty("Idempotency-Key", idempotencyKey)
            }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }

        try {
            if (body != null) {
                connection.outputStream.bufferedWriter().use { writer ->
                    writer.write(body.toString())
                }
            }

            val responseCode = connection.responseCode
            val responseBody = readResponseBody(connection, responseCode)

            if (responseCode in 200..299) {
                return@withContext if (responseBody.isBlank()) JSONObject() else JSONObject(responseBody)
            }

            val errorPayload = responseBody.takeIf { it.isNotBlank() }?.let(::JSONObject)
            val errorCode = errorPayload?.optString("errorCode").orEmpty()
            val errorMessage = errorPayload?.optString("message").orEmpty()

            if (errorCode.isNotBlank() && errorMessage.isNotBlank()) {
                throw BackendApiBusinessException(
                    backendCode = errorCode,
                    message = errorMessage,
                )
            }

            throw NetworkHttpException(
                code = responseCode,
                payload = responseBody.ifBlank { null },
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun buildUrl(
        path: String,
        query: Map<String, String?>,
    ): String {
        val normalizedPath = if (path.startsWith('/')) path else "/$path"
        val base = baseUrl.removeSuffix("/")
        val encodedQuery = query.entries
            .filter { !it.value.isNullOrBlank() }
            .joinToString("&") { (key, value) ->
                val encodedValue = URLEncoder.encode(value.orEmpty(), Charsets.UTF_8.name())
                "${URLEncoder.encode(key, Charsets.UTF_8.name())}=$encodedValue"
            }

        return if (encodedQuery.isBlank()) "$base$normalizedPath" else "$base$normalizedPath?$encodedQuery"
    }

    private fun readResponseBody(
        connection: HttpURLConnection,
        responseCode: Int,
    ): String {
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        if (stream == null) return ""
        return stream.bufferedReader().use { it.readText() }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 20_000
    }
}

data class BackendApiBusinessException(
    val backendCode: String,
    override val message: String,
) : RuntimeException(message)
