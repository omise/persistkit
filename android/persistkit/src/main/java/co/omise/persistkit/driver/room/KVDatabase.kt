package co.omise.persistkit.driver.room

import androidx.room.Database
import androidx.room.RoomDatabase
import co.omise.persistkit.Record

@Database(entities = [Record::class], version = 1, exportSchema = false)
abstract class KVDatabase : RoomDatabase() {
    abstract fun queries(): Queries
}
