package co.omise.persistkit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface Queries {
    @Query("SELECT * FROM records;")
    fun loadAll(): List<Record>

    @Query("SELECT * FROM records WHERE _id = :identifier;")
    fun load(identifier: String): List<Record>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(rec: Record)

    @Query("DELETE FROM records WHERE _id = :identifier")
    fun delete(identifier: String): Int
}
