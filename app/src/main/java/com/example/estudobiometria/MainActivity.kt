package com.example.estudobiometria

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import br.com.havan.feature_card_authentication.common.utils.getCipher
import br.com.havan.feature_card_authentication.common.utils.getSecretKey
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val biometricFragment = BiometricFragment()
        addFragmentToActivity(biometricFragment)
    }
    private fun addFragmentToActivity(fragment: Fragment?){
        if (fragment == null) return
        val fm = supportFragmentManager
        val tr = fm.beginTransaction()
        tr.add(R.id.baseContainer, fragment)
        tr.commitAllowingStateLoss()
    }


}