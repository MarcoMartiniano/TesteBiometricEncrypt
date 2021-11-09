package br.com.havan.feature_card_authentication.common.utils

import android.content.Context

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

private val keyStoreAlias = "keyStoreAlias"
private val CONSTANT_CIPHER_IV = " bVQzNFNhRkQ1Njc4UUFaWA=="

fun createBiometricCallback (
    onAuthenticationError: () -> Unit = {},
    onAuthenticationSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit
) = @RequiresApi(Build.VERSION_CODES.P)
object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        onAuthenticationError.invoke()
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onAuthenticationSuccess.invoke(result)
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        onAuthenticationError.invoke()
    }
}

fun getBiometricPromptBuilder(context: Context) = context.run {
    BiometricPrompt.PromptInfo.Builder()
        .setTitle("Prompt Test")
        .setConfirmationRequired(true)
        .setNegativeButtonText("Cancel")
        .build()
}


fun Fragment.createBiometricPrompt(authCallBack: BiometricPrompt.AuthenticationCallback) =
    BiometricPrompt(
        requireActivity(),
        ContextCompat.getMainExecutor(context!!),
        authCallBack
    )

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
fun Fragment.canUseBiometricAuthentication(): Boolean {
    context?.let { context -> 
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    } ?: return false
}

fun BiometricPrompt.authenticateWithBiometric(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (isKeyAlreadyCreated().not()) {
            generateSecretKey()
        }

        val ivParameterSpec = IvParameterSpec(Base64.decode(CONSTANT_CIPHER_IV, Base64.DEFAULT))
        val cipher = getCipher()
        val secretKey = getSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        authenticate(
            getBiometricPromptBuilder(context),
            BiometricPrompt.CryptoObject(cipher)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private fun generateSecretKey() {
    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    keyGenerator.init(
        KeyGenParameterSpec.Builder(
            keyStoreAlias,
            KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setRandomizedEncryptionRequired(false)
            .build()
    )
    keyGenerator.generateKey()
}

@RequiresApi(Build.VERSION_CODES.M)
fun getSecretKey(): SecretKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)
    return keyStore.getKey(keyStoreAlias, null) as SecretKey
}

@RequiresApi(Build.VERSION_CODES.M)
fun getCipher(): Cipher {
    return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
            + KeyProperties.BLOCK_MODE_CBC + "/"
            + KeyProperties.ENCRYPTION_PADDING_PKCS7)
}

@RequiresApi(Build.VERSION_CODES.M)
fun isKeyAlreadyCreated(): Boolean {
    return try {
        getSecretKey()
        true
    } catch (e: Exception) {
        false
    }
}