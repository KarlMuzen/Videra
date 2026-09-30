package io.github.shashigm.videra.data.remote.api

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.github.shashigm.videra.data.remote.dto.AddonManifestDto
import io.github.shashigm.videra.data.remote.dto.MediaItemDto
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url

interface VideraAddonApi {

    @GET
    suspend fun getManifest(
        @Url url: String
    ): AddonManifestDto

    @GET
    suspend fun getMediaItems(
        @Url url: String
    ): List<MediaItemDto>

 {
        fun create(
            httpClient: OkHttpClient = OkHttpClient(),
            json: Json = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        ): VideraAddonApi {
            val contentType = "application/json".toMediaType()

            return Retrofit.Builder()
                // @Url methods receive absolute URLs at call time.
                // This placeholder only satisfies Retrofit's baseUrl requirement.
                .baseUrl("https://videra.invalid/")
                .client(httpClient)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(VideraAddonApi::class.java)
        }
    }
}
