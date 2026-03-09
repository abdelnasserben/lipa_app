package com.kori.app.core.network

import java.io.IOException
import java.net.SocketTimeoutException

sealed interface NetworkError {
    data class Http(val code: Int, val payload: String?) : NetworkError
    data class BackendBusiness(val code: String, val message: String) : NetworkError
    data class Parsing(val causeMessage: String?) : NetworkError
    data class Connectivity(val causeMessage: String?) : NetworkError
    data class Timeout(val causeMessage: String?) : NetworkError
    data class Unknown(val causeMessage: String?) : NetworkError
}

data class NetworkErrorPresentation(
    val technicalMessage: String,
    val uxMessage: String,
)

class BackendBusinessException(
    val backendCode: String,
    override val message: String,
) : RuntimeException(message)

object NetworkErrorMapper {
    fun fromThrowable(throwable: Throwable): NetworkError {
        return when (throwable) {
            is BackendBusinessException -> NetworkError.BackendBusiness(throwable.backendCode, throwable.message)
            is SocketTimeoutException -> NetworkError.Timeout(throwable.message)
            is IOException -> NetworkError.Connectivity(throwable.message)
            is kotlinx.serialization.SerializationException,
            is IllegalArgumentException,
            -> NetworkError.Parsing(throwable.message)
            else -> NetworkError.Unknown(throwable.message)
        }
    }

    fun toPresentation(error: NetworkError): NetworkErrorPresentation {
        return when (error) {
            is NetworkError.Http -> NetworkErrorPresentation(
                technicalMessage = "HTTP_${error.code}: ${error.payload.orEmpty()}",
                uxMessage = "Le service est momentanément indisponible.",
            )

            is NetworkError.BackendBusiness -> NetworkErrorPresentation(
                technicalMessage = "BUSINESS_${error.code}: ${error.message}",
                uxMessage = error.message,
            )

            is NetworkError.Parsing -> NetworkErrorPresentation(
                technicalMessage = "PARSING: ${error.causeMessage.orEmpty()}",
                uxMessage = "Une réponse inattendue a été reçue.",
            )

            is NetworkError.Connectivity -> NetworkErrorPresentation(
                technicalMessage = "CONNECTIVITY: ${error.causeMessage.orEmpty()}",
                uxMessage = "Vérifie ta connexion internet puis réessaie.",
            )

            is NetworkError.Timeout -> NetworkErrorPresentation(
                technicalMessage = "TIMEOUT: ${error.causeMessage.orEmpty()}",
                uxMessage = "La requête a expiré, réessaie dans un instant.",
            )

            is NetworkError.Unknown -> NetworkErrorPresentation(
                technicalMessage = "UNKNOWN: ${error.causeMessage.orEmpty()}",
                uxMessage = "Une erreur inattendue est survenue.",
            )
        }
    }
}
