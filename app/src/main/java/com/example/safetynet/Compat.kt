package com.example.safetynet

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

@Suppress("NOTHING_TO_INLINE")
inline fun Context.getColorCompat(@ColorRes id: Int) = ContextCompat.getColor(this, id)