package com.example.safetynet.utils

sealed class SafetyNetResult

data class Success(
    val ctsProfileMatch: Boolean,
    val basicIntegrity: Boolean
) : SafetyNetResult()

data class Error(
    val errorCode: Int,
    val errorMessage: String
) : SafetyNetResult()