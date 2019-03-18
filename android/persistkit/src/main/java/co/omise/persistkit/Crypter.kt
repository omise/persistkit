package co.omise.persistkit

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.VisibleForTesting
import java.security.KeyStore
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import java.security.Key

private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val TRANSFORMATION_SYMMETRIC = "AES/GCM/NoPadding"

// IV (Initialization Vector) has fix size 12 bytes for GCM modes
private const val IV = "co.omise.ivx"

private var forceCreateKeyStore: Boolean = false

open class Crypter(private val aliasKeyName: String) {
    lateinit var keyStore: KeyStore

    private val cipher: Cipher by lazy { Cipher.getInstance(TRANSFORMATION_SYMMETRIC) }

    val key: Key by lazy {
        try {
            keyStore.getKey(aliasKeyName, null)
        } catch (e: UnrecoverableKeyException) {
            forceCreateKeyStore = true
            throw e
        }
    }

    private val parameterSpec: GCMParameterSpec by lazy { GCMParameterSpec(128, IV.toByteArray()) }

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
        val keySpec = KeyGenParameterSpec.Builder(aliasKeyName, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
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
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)
        return cipher.doFinal(plainData)
    }

    open fun decrypt(encryptedBytes: ByteArray): ByteArray{
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return decryptedBytes
    }
}
