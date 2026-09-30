package io.github.shashigm.videra.domain.usecase

import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.AddonManifest
import io.github.shashigm.videra.domain.repository.AddonRepository
import java.net.URI

class InstallAddonUseCase(
    private val repository: AddonRepository
) {

    suspend operator fun invoke(manifestUrl: String): Resource<AddonManifest> {
        val normalizedUrl = manifestUrl.trim()
        if (!isValidHttpUrl(normalizedUrl)) {
            return Resource.Error(
                IllegalArgumentException(
                    "Enter a valid HTTP or HTTPS add-on manifest URL."
                )
            )
        }

        return repository.installAddon(
            baseUrl = normalizedUrl,
            manifestUrl = normalizedUrl
        )
    }

    private fun isValidHttpUrl(url: String): Boolean {
        val parsed = runCatching { URI(url) }.getOrNull() ?: return false
        val scheme = parsed.scheme?.lowercase()
        return (scheme == "http" || scheme == "https") &&
            !parsed.host.isNullOrBlank()
    }
}