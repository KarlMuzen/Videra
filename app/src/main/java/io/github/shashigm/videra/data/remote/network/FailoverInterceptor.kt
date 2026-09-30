package io.github.shashigm.videra.data.remote.network

import java.io.IOException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class FailoverInterceptor(
    backupApiBaseUrls: List<String>
) : Interceptor {

    private val backupBaseUrls: List<HttpUrl> =
        backupApiBaseUrls
            .distinct()
            .map { baseUrl ->
                baseUrl.trimEnd('/').toHttpUrl()
            }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        if (backupBaseUrls.isEmpty()) {
            return chain.proceed(originalRequest)
        }

        var lastIOException: IOException? = null
        var lastRetryableStatus: Int? = null

        // First attempt always uses the exact absolute URL supplied through @Url.
        // Backup URLs replace only the scheme/host/port; path and query are preserved.
        val attemptUrls = buildList {
            add(originalUrl)

            backupBaseUrls
                .map { backupBaseUrl ->
                    originalUrl.withBackupOrigin(backupBaseUrl)
                }
                .filterNot { it == originalUrl }
                .distinct()
                .forEach(::add)
        }

        for (attemptUrl in attemptUrls) {
            val attemptRequest = originalRequest
                .newBuilder()
                .url(attemptUrl)
                .build()

            try {
                val response = chain.proceed(attemptRequest)

                if (!isRetryableStatus(response.code)) {
                    return response
                }

                lastRetryableStatus = response.code
                response.close()
            } catch (exception: IOException) {
                lastIOException = exception
            }
        }

        val message = buildString {
            append("All Videra add-on endpoints failed")

            lastRetryableStatus?.let {
                append(". Last HTTP status: ")
                append(it)
            }

            lastIOException?.let {
                append(". Last network error: ")
                append(it.message ?: it.javaClass.simpleName)
            }
        }

        throw IOException(message, lastIOException)
    }

    private fun isRetryableStatus(code: Int): Boolean {
        return code == 408 ||
            code == 429 ||
            code in 500..599
    }

    private fun HttpUrl.withBackupOrigin(
        backupBaseUrl: HttpUrl
    ): HttpUrl {
        return newBuilder()
            .scheme(backupBaseUrl.scheme)
            .host(backupBaseUrl.host)
            .port(backupBaseUrl.port)
            .build()
    }
}
