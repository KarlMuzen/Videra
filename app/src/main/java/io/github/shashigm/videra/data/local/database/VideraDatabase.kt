package io.github.shashigm.videra.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.shashigm.videra.data.local.dao.AddonDao
import io.github.shashigm.videra.data.local.entity.InstalledAddonEntity

@Database(
    entities = [InstalledAddonEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VideraDatabase : RoomDatabase() {

    abstract fun addonDao(): AddonDao

    companion object {
        fun create(context: Context): VideraDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VideraDatabase::class.java,
                "videra.db"
            ).build()
        }
    }
}
