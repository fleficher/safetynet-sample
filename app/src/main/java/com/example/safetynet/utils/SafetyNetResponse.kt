package com.example.safetynet.utils


internal data class SafetyNetResponse(
    val nonce: String? = null,
    val apkPackageName: String? = null,
    val apkCertificateDigestSha256: List<String>? = null,
    val ctsProfileMatch: Boolean = false,
    val basicIntegrity: Boolean = false
)