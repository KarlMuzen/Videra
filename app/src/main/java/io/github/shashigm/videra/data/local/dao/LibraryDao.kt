package io.github.shashigm.videra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.shashigm.videra.data.local.entity.LibraryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: LibraryEntity)

    @Query(
        "SELECT * FROM library_items " +
            "ORDER BY savedAt DESC"
    )
    fun observeSavedItems(): Flow<List<LibraryEntity>>

    @Query(
        "DELETE FROM library_items WHERE libraryKey = :libraryKey"
    )
    suspend fun deleteByKey(libraryKey: String)
}
