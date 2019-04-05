package co.omise.persistkit

import co.omise.persistkit.driver.Driver
import org.apache.commons.lang3.SerializationUtils
import java.io.Serializable

open class BasicDatabase(private val driver: Driver) : Database {
    override fun <T> loadAll(): List<T> where T : Identifiable, T : Serializable {
        return driver.query(Command.LoadAll)
            .map { decode<T>(it) }
    }

    override fun <T> load(identifier: String): T? where T : Identifiable, T : Serializable {
        return driver.query(Command.Load(identifier))
            .map { decode<T>(it) }
            .firstOrNull()
    }

    override fun <T> load(identifiers: List<String>): List<T> where T : Identifiable, T : Serializable {
        return driver.query(Command.LoadWithIDs(identifiers))
            .map { decode<T>(it) }
    }

    override fun <T> save(obj: T) where T : Identifiable, T : Serializable {
        driver.execute(Command.Save(encode(obj)))
    }

    override fun delete(identifier: String): Boolean {
        return driver.execute(Command.Delete(identifier)) == 1
    }

    protected open fun <T> decode(rec: Record): T where T : Identifiable, T : Serializable {
        return SerializationUtils.deserialize(rec.content)
    }

    protected open fun <T> encode(obj: T): Record where T : Identifiable, T : Serializable {
        val content = SerializationUtils.serialize(obj)
        return Record(obj.identifier, obj::class.java.simpleName, 0, content)
    }
}
