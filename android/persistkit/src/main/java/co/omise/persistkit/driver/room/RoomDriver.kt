package co.omise.persistkit.driver.room;

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import co.omise.persistkit.Command;
import co.omise.persistkit.Record
import co.omise.persistkit.driver.Driver
import co.omise.persistkit.driver.exception.UnsupportedCommandException
import org.jetbrains.annotations.TestOnly


class RoomDriver(context: Context, filename: String) : Driver, ContextWrapper(context) {
    val db: KVDatabase = Room.databaseBuilder(context, KVDatabase::class.java, filename)
        .build()

    override fun query(command: Command): List<Record> {
        return when (command) {
            is Command.LoadAll -> db.queries().loadAll()
            is Command.Load -> db.queries().load(command.identifier)
            is Command.LoadWithIDs -> db.queries().load(command.identifiers)
            else -> throw UnsupportedCommandException(command)
        }
    }

    override fun execute(command: Command): Int {
        return when (command) {
            is Command.LoadAll -> db.queries().loadAll().size
            is Command.Load -> db.queries().load(command.identifier).size
            is Command.LoadWithIDs -> db.queries().load(command.identifiers).size
            is Command.Delete -> db.queries().delete(command.identifier)
            is Command.Save -> {
                db.queries().save(command.record)
                1
            }
        }
    }

    @TestOnly
    fun deleteAll() {
        db.queries().deleteAll()
    }
}
