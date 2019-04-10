package co.omise.persistkit

import co.omise.persistkit.driver.Driver
import java.io.Serializable

class EncryptedDatabase(private val driver: Driver, private val aliasKeyName: String) : BasicDatabase(driver),
    Database {
    private val crypter = Crypter(aliasKeyName)

    override fun <T> decode(rec: Record): T where T : Identifiable, T : Serializable {
        rec.content = crypter.decrypt(rec.content)
        return super.decode(rec)
    }

    override fun <T> encode(obj: T): Record where T : Identifiable, T : Serializable {
        val record = super.encode(obj)
        record.content = crypter.encrypt(record.content)
        return record
    }
}
