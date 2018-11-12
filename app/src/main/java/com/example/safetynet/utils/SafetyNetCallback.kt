package com.example.safetynet.utils

/**
 * Callback for retrieving safetynet api result
 */
interface SafetyNetCallback {

    fun onError(errorCode: Int, errorMessage: String)

    /**
     * @param ctsProfileMatch iF true, then the profile of the device running your app matches
     * the profile of a device that has passed Android compatibility testing.
     * @param basicIntegrity if true, then the device running the app likely wasn't tampered with,
     * but the device hasn't necessarily passed Android compatibility testing.
     */
    fun onSuccess(ctsProfileMatch: Boolean, basicIntegrity: Boolean)
}