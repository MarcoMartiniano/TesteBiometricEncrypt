package com.example.estudobiometria

import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import br.com.havan.feature_card_authentication.common.utils.*
import com.example.estudobiometria.databinding.FragmentBiometricBinding
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec


class BiometricFragment : Fragment() {

    private val CONSTANT_CIPHER_IV = " bVQzNFNhRkQ1Njc4UUFaWA=="
    private val binding by lazy { FragmentBiometricBinding.inflate(layoutInflater) }

    private var encryptoData: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btEncryptography.setOnClickListener {
            setUp("Batatinha frita 1 2 3")
        }
        binding.btUncryptography.setOnClickListener {
            binding.tvUncryptography.text = decryptPassword(encryptoData)
        }
    }


    private fun setUp(userPassword: String){
        if (canUseBiometricAuthentication()) {
            Toast.makeText(context,"Can use Authentication",Toast.LENGTH_LONG).show()
            createBiometricPrompt(
                createAuthenticationCallback(userPassword)
            ).authenticateWithBiometric(
                context = requireContext()
            )
        }else{
            Toast.makeText(context,"Can´t use Authentication",Toast.LENGTH_LONG).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun decryptPassword(data: String): String {
        if (canUseBiometricAuthentication()) {
            val cipher = getCipher()
            val ivParameterSpec =
                IvParameterSpec(Base64.decode(CONSTANT_CIPHER_IV, Base64.DEFAULT))
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), ivParameterSpec)
            return String(
                cipher.doFinal(
                    Base64.decode(
                        data ?: "",
                        Base64.DEFAULT
                    )
                )
            )
        }
        return ""
    }

    private fun createAuthenticationCallback(userPassword: String) = createBiometricCallback(
        onAuthenticationSuccess = { result ->
            val encryptedPassword = Base64.encodeToString(
                result.cryptoObject?.cipher?.doFinal(
                    (userPassword ?: "").toByteArray(Charsets.UTF_8)
                ),
                Base64.DEFAULT
            )
            binding.tvEncryptography.text = encryptedPassword
            encryptoData = encryptedPassword
        },
        onAuthenticationError = {
            binding.tvEncryptography.text = "Error Authentication"
        }
    )
}