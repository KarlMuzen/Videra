package io.github.shashigm.videra.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.shashigm.videra.data.local.dao.AddonDao
import io.github.shashigm.videra.data.local.entity.InstalledAddonEntity

@Database(
    entities = [InstalledAddonEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VideraDatabase : RoomDatabase() {

    abstract fun addonDao(): AddonDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE installed_addons ADD COLUMN mediaItemsUrl TEXT"
                )
            }
        }

        fun create(context: Context): VideraDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                VideraDatabase::class.java,
                "videra.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
