package co.omise.persister

import org.apache.commons.lang3.SerializationUtils
import java.io.Serializable

class Storage(val db: KVDatabase) {
    fun <T> loadAll(): List<T> where T : Identifiable, T : Serializable {
        return db.queries()
            .listAll()
            .map { decode<T>(it) }
            .reversed()
    }

    fun <T> save(obj: T) where T : Identifiable, T : Serializable {
        db.queries().save(encode(obj))
    }

    private fun <T> decode(rec: Record): T where T : Identifiable, T : Serializable {
        return SerializationUtils.deserialize(rec.content)
    }

    private fun <T> encode(obj: T): Record where T : Identifiable, T : Serializable {
        val content = SerializationUtils.serialize(obj)
        return Record(obj.identifier, obj::class.java.simpleName, 0, content)
    }
}