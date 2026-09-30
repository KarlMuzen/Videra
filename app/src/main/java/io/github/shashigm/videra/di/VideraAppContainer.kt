package io.github.shashigm.videra.di

import android.content.Context
import io.github.shashigm.videra.data.local.database.VideraDatabase
import io.github.shashigm.videra.data.remote.api.VideraAddonApi
import io.github.shashigm.videra.data.repository.AddonRepositoryImpl
import io.github.shashigm.videra.domain.repository.AddonRepository

class VideraAppContainer(
    context: Context
) {
    private val database: VideraDatabase =
        VideraDatabase.create(context.applicationContext)

    private val api: VideraAddonApi =
        VideraAddonApi.create()

    val addonRepository: AddonRepository by lazy {
        AddonRepositoryImpl(
            api = api,
            addonDao = database.addonDao()
        )
    }
}
