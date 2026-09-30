package io.github.shashigm.videra.di

import android.content.Context
import io.github.shashigm.videra.data.local.database.VideraDatabase
import io.github.shashigm.videra.data.preferences.PreferencesRepository
import io.github.shashigm.videra.data.remote.api.VideraAddonApi
import io.github.shashigm.videra.data.repository.AddonRepositoryImpl
import io.github.shashigm.videra.data.repository.EpisodeProgressRepositoryImpl
import io.github.shashigm.videra.data.repository.LibraryRepositoryImpl
import io.github.shashigm.videra.domain.repository.AddonRepository
import io.github.shashigm.videra.domain.repository.EpisodeProgressRepository
import io.github.shashigm.videra.domain.repository.LibraryRepository
import io.github.shashigm.videra.domain.usecase.InstallAddonUseCase
import io.github.shashigm.videra.media.player.PlayerController

class VideraAppContainer(
    context: Context
) {
    private val applicationContext = context.applicationContext

    private val database: VideraDatabase =
        VideraDatabase.create(applicationContext)

    private val api: VideraAddonApi =
        VideraAddonApi.create()

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(applicationContext)
    }

    val addonRepository: AddonRepository by lazy {
        AddonRepositoryImpl(
            api = api,
            addonDao = database.addonDao()
        )
    }

    val installAddonUseCase: InstallAddonUseCase by lazy {
        InstallAddonUseCase(addonRepository)
    }

    val episodeProgressRepository: EpisodeProgressRepository by lazy {
        EpisodeProgressRepositoryImpl(
            dao = database.episodeProgressDao()
        )
    }

    val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(
            libraryDao = database.libraryDao()
        )
    }

    val playerController: PlayerController by lazy {
        PlayerController(
            context = applicationContext
        )
    }
}
