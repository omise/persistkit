package co.omise.persister

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Record::class], version = 1, exportSchema = false)
abstract class KVDatabase : RoomDatabase() {
    abstract fun queries(): Queries
}