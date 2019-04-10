package co.omise.persistkit

import java.io.Serializable

interface Database {
    fun <T> loadAll(): List<T> where T : Identifiable, T : Serializable

    fun <T> load(identifier: String): T? where T : Identifiable, T : Serializable

    fun <T> load(identifiers: List<String>): List<T> where T : Identifiable, T : Serializable

    fun <T> save(obj: T) where T : Identifiable, T : Serializable

    fun delete(identifier: String): Boolean

    fun deleteDatabase(): Boolean
}
