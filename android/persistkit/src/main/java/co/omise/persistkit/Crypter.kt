package co.omise.persistkit

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.VisibleForTesting
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.Key
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

private var forceCreateKeyStore: Boolean = false

open class Crypter(private val aliasKeyName: String) {
    lateinit var keyStore: KeyStore

    val key: Key by lazy {
        try {
            keyStore.getKey(aliasKeyName, null)
        } catch (e: UnrecoverableKeyException) {
            forceCreateKeyStore = true
            throw e
        }
    }

    init {
        createAndroidKeyStore()
    }

    private fun createAndroidKeyStore() {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        if (keyStore.containsAlias(aliasKeyName) && !forceCreateKeyStore) {
            return
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val keySpec =
            KeyGenParameterSpec.Builder(aliasKeyName, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build()
        keyGenerator.init(keySpec)
        keyGenerator.generateKey()
        forceCreateKeyStore = false
    }

    @VisibleForTesting
    fun deleteAndroidKeyStore() {
        keyStore.deleteEntry(aliasKeyName)
    }

    open fun encrypt(plainData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION_SYMMETRIC)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val byteArrayOutputStream = ByteArrayOutputStream()
        val byteArrayInputStream = ByteArrayInputStream(plainData)
        val cipherInputStream = CipherOutputStream(byteArrayOutputStream, cipher)
        val buffer = ByteArray(BUFFER_SIZE)
        var read = byteArrayInputStream.read(buffer)
        while (read != -1) {
            cipherInputStream.write(buffer, 0, read)
            read = byteArrayInputStream.read(buffer)
        }
        byteArrayInputStream.close()
        byteArrayOutputStream.flush()
        cipherInputStream.close()

        val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)
        val encryptedData = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
        val encryptedDataWithIV = iv + IV_SEPARATOR + encryptedData
        return encryptedDataWithIV.toByteArray()
    }

    open fun decrypt(encryptedBytes: ByteArray): ByteArray {
        val splited = String(encryptedBytes).split(IV_SEPARATOR.toRegex())

        if (splited.size != 2) {
            throw IllegalArgumentException("Passed data is incorrect. There was no IV specified with it.")
        }

        val iv = Base64.decode(splited[0], Base64.DEFAULT)
        val data = Base64.decode(splited[1], Base64.DEFAULT)

        val cipher = Cipher.getInstance(TRANSFORMATION_SYMMETRIC)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))
        val inputStream = ByteArrayInputStream(data)
        val cipherInputStream = CipherInputStream(inputStream, cipher)
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        var numberOfBytedRead = cipherInputStream.read(buffer)
        while (numberOfBytedRead >= 0) {
            outputStream.write(buffer, 0, numberOfBytedRead)
            numberOfBytedRead = cipherInputStream.read(buffer)
        }
        return outputStream.toByteArray()
    }

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val TRANSFORMATION_SYMMETRIC = "AES/GCM/NoPadding"
        private const val TAG_LENGTH = 128
        private const val IV_SEPARATOR = "]"
        private const val BUFFER_SIZE = 1024
    }
}
