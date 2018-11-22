package co.omise.persister

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface Queries {
    @Query("SELECT * FROM records;")
    fun listAll(): List<Record>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(rec: Record)
}