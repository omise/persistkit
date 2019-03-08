package co.omise.persistkit

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import org.apache.commons.lang3.SerializationUtils
import java.io.Serializable

class Storage(val context: Context) : ContextWrapper(context) {
    val db: KVDatabase = Room.databaseBuilder(context, KVDatabase::class.java, "persistkit")
        .build()

    fun <T> loadAll(): List<T> where T : Identifiable, T : Serializable {
        return db.queries()
            .loadAll()
            .map { decode<T>(it) }
            .reversed()
    }

    fun <T> load(identifier: String): T where T : Identifiable, T : Serializable {
        return db.queries()
            .load(identifier)
            .map { decode<T>(it) }
            .first()
    }

    fun <T> save(obj: T) where T : Identifiable, T : Serializable {
        db.queries().save(encode(obj))
    }

    fun delete(identifier: String) {
        db.queries().delete(identifier)
    }

    private fun <T> decode(rec: Record): T where T : Identifiable, T : Serializable {
        return SerializationUtils.deserialize(rec.content)
    }

    private fun <T> encode(obj: T): Record where T : Identifiable, T : Serializable {
        val content = SerializationUtils.serialize(obj)
        return Record(obj.identifier, obj::class.java.simpleName, 0, content)
    }
}