package com.example.safetynet.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.os.Build.VERSION
import android.util.Base64
import timber.log.Timber
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


internal object SafetyNetUtils {

    private const val SHA_256 = "SHA-256"

    @Suppress("DEPRECATION")
    private val infoFlag: Int =
        if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }

    fun calcApkCertificateDigests(context: Context): List<String>? {
        val encodedSignatures = ArrayList<String>()

        // Get signatures from package manager
        val packageInfo = getPackageInfo(context) ?: return null
        val signatures = getSignatures(packageInfo)

        // Calculate base64 encoded sha256 hash of signatures
        val messageDigest = MessageDigest.getInstance(SHA_256)
        signatures.forEach {
            try {
                messageDigest.update(it.toByteArray())
                val digest = messageDigest.digest()
                encodedSignatures.add(Base64.encodeToString(digest, Base64.NO_WRAP))
            } catch (e: NoSuchAlgorithmException) {
                Timber.e(e)
            }
        }

        return encodedSignatures
    }

    private fun getPackageInfo(context: Context): PackageInfo? {
        val pm = context.packageManager
        val packageName = context.packageName
        return try {
            pm.getPackageInfo(packageName, infoFlag)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            null
        }
    }

    private fun getSignatures(packageInfo: PackageInfo): Array<Signature> {
        return if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            packageInfo.signatures
        }
    }

}