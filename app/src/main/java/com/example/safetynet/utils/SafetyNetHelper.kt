package com.example.safetynet.utils

import android.content.Context
import android.util.Base64
import com.example.safetynet.BuildConfig
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import timber.log.Timber
import java.security.SecureRandom
import java.util.*

class SafetyNetHelper(
    private val context: Context,
    private val callback: SafetyNetCallback
) {

    companion object {
        const val RESPONSE_VALIDATION_FAILED = 1000

        private const val API_KEY = BuildConfig.SAFETY_NET_API_KEY
    }

    private val secureRandom = SecureRandom()
    private val parser by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    private val packageName: String? = context.packageName
    private val apkCertificateDigests: List<String>? by lazy {
        SafetyNetUtils.calcApkCertificateDigests(
            context
        )
    }

    /**
     * Call the SafetyNet test to check if this device profile/ROM has passed the CTS test
     */
    fun requestAttestation() {
        val requestNonce = generateOneTimeRequestNonce()

        SafetyNet.getClient(context).attest(requestNonce, API_KEY)
            .addOnSuccessListener { it -> onRequestSuccess(it, requestNonce) }
            .addOnFailureListener { it -> onRequestFail(it) }
    }

    private fun onRequestSuccess(
        apiResponse: SafetyNetApi.AttestationResponse,
        requestNonce: ByteArray
    ) {
        val jwsResult = apiResponse.jwsResult
        val response = parseJsonWebSignature(jwsResult) ?: return

        if (!response.ctsProfileMatch || !response.basicIntegrity) {
            callback.onSuccess(response.ctsProfileMatch, response.basicIntegrity)
            return
        } else {
            // If ctsProfileMatch & basicIntegrity are true, we need to check if the response hasn't been tampered
            if (validateSafetyNetResponsePayload(requestNonce, response)) {
                callback.onSuccess(response.ctsProfileMatch, response.basicIntegrity)
            } else {
                callback.onError(
                    RESPONSE_VALIDATION_FAILED,
                    "Response payload validation failed"
                )
            }
        }
    }

    private fun onRequestFail(exception: Exception) {
        if (exception is ApiException) {
            callback.onError(
                exception.statusCode, "ApiException: ${exception.message}"
            )
        } else {
            Timber.d(exception)
            callback.onError(RESPONSE_VALIDATION_FAILED, "Response payload validation failed")
        }
    }

    private fun parseJsonWebSignature(jwsResult: String?): SafetyNetResponse? {
        val jwtParts = jwsResult?.split("\\.".toRegex())?.dropLastWhile { it.isEmpty() }
        return jwtParts?.takeIf { it.size >= 2 }?.let {
            // We're only interested in the body/payload
            val decodedPayload = String(Base64.decode(it[1], Base64.DEFAULT))
            parser.adapter(SafetyNetResponse::class.java).fromJson(decodedPayload)
        }
    }

    /**
     * Generate a random request nonce.
     *  Include as many pieces of data in the nonce as possible.
     *  In doing so, you make it more difficult for attackers to carry out replay attacks.
     *  For example, deriving the nonce from the username limits replay attacks to the same account.
     *  However, deriving the nonce from all the details of a purchase event limits the attestation result to that purchase event only.
     */
    private fun generateOneTimeRequestNonce(): ByteArray {
        return ByteArray(32).apply {
            secureRandom.nextBytes(this)
        }
    }

    /**
     * Verify the compatibility check response.
     * It's not recommended to perform the verification directly in your app because,
     * in that case, there is no guarantee that the verification logic itself hasn't been modified.
     */
    private fun validateSafetyNetResponsePayload(
        requestNonce: ByteArray,
        response: SafetyNetResponse?
    ): Boolean {
        if (response == null) {
            Timber.e("SafetyNetResponse is null.")
            return false
        }

        // check the request nonce matched the response
        val requestNonceBase64 =
            Base64.encodeToString(requestNonce, Base64.DEFAULT).trim { it <= ' ' }

        if (requestNonceBase64 != response.nonce) {
            Timber.e(
                "invalid nonce, expected = \"$requestNonceBase64\", response = \"${response.nonce}\""
            )
            return false
        }

        if (!packageName.equals(response.apkPackageName, ignoreCase = true)) {
            Timber.e(
                "invalid packageName, expected = \"$packageName\", response = \"${response.apkPackageName}\""
            )
            return false
        }

        if (apkCertificateDigests?.equals(response.apkCertificateDigestSha256) != true) {
            Timber.e(
                "invalid apkCertificateDigest, expected = %s, response = %s",
                Arrays.asList(apkCertificateDigests).toString(),
                Arrays.asList(response.apkCertificateDigestSha256).toString()
            )
            return false
        }

        return true
    }
}