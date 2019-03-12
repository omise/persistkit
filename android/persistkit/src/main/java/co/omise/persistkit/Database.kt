package co.omise.persistkit

import co.omise.persistkit.driver.Driver
import org.apache.commons.lang3.SerializationUtils
import java.io.Serializable

class Database(private val driver: Driver) {
    fun <T> loadAll(): List<T> where T : Identifiable, T : Serializable {
        return driver.query(Command.LoadAll)
            .map { decode<T>(it) }
    }

    fun <T> load(identifier: String): T where T : Identifiable, T : Serializable {
        return driver.query(Command.Load(identifier))
            .map { decode<T>(it) }
            .first()
    }

    fun <T> save(obj: T) where T : Identifiable, T : Serializable {
        driver.execute(Command.Save(encode(obj)))
    }

    fun delete(identifier: String): Boolean {
        return driver.execute(Command.Delete(identifier)) == 1
    }

    private fun <T> decode(rec: Record): T where T : Identifiable, T : Serializable {
        return SerializationUtils.deserialize(rec.content)
    }

    private fun <T> encode(obj: T): Record where T : Identifiable, T : Serializable {
        val content = SerializationUtils.serialize(obj)
        return Record(obj.identifier, obj::class.java.simpleName, 0, content)
    }
}
