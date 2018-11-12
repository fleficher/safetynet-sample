package com.example.safetynet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.safetynet.utils.Error
import com.example.safetynet.utils.SafetyNetHelper
import com.example.safetynet.utils.Success
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val safetyNetHelper by lazy {
        SafetyNetHelper(applicationContext)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launch {
            val result = async(Dispatchers.IO) { safetyNetHelper.requestAttestation() }.await()
            when (result) {
                is Success -> onSuccess(result.ctsProfileMatch, result.basicIntegrity)
                is Error -> onError(result.errorMessage)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun onError(errorMessage: String) {
        rootResult.text = getString(R.string.safetynet_response_error)
            .format(Locale.getDefault(), errorMessage)
    }

    private fun onSuccess(ctsProfileMatch: Boolean, basicIntegrity: Boolean) {
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
