package com.example.safetynet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.safetynet.utils.SafetyNetCallback
import com.example.safetynet.utils.SafetyNetHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), SafetyNetCallback {

    private val safetyNetHelper by lazy {
        SafetyNetHelper(applicationContext, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        safetyNetHelper.requestAttestation()
    }

    override fun onError(errorCode: Int, errorMessage: String) {
        rootResult.text = getString(R.string.safetynet_response_error)
            .format(Locale.getDefault(), errorMessage)
    }

    override fun onSuccess(ctsProfileMatch: Boolean, basicIntegrity: Boolean) {
        if (ctsProfileMatch && basicIntegrity) {
            rootResult.text = getString(R.string.safetynet_response_ok)
            rootResult.setTextColor(getColorCompat(R.color.green))
        } else if (!ctsProfileMatch && basicIntegrity) {
            rootResult.text = getString(R.string.safetynet_response_uncertified)
            rootResult.setTextColor(getColorCompat(R.color.orange))
        } else {
            rootResult.text = getString(R.string.safetynet_response_root)
            rootResult.setTextColor(getColorCompat(R.color.red))
        }
    }
}
