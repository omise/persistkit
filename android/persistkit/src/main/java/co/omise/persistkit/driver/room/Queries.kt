package co.omise.persistkit.driver.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.omise.persistkit.Record


@Dao
interface Queries {
    @Query("SELECT * FROM records;")
    fun loadAll(): List<Record>

    @Query("SELECT * FROM records WHERE _id = :identifier;")
    fun load(identifier: String): List<Record>

    // NOTE: REPLACE Strategy would make `loadAll` inconsistence
    // since it's create new row instead of update.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(rec: Record): Long

    @Query("DELETE FROM records WHERE _id = :identifier")
    fun delete(identifier: String): Int
}
