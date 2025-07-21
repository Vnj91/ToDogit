package com.example.todo91.ui.theme

import androidx.compose.ui.graphics.Color

fun Color.toArgb(): Int {
    return (alpha * 255.0f + 0.5f).toInt() shl 24 or
            (red * 255.0f + 0.5f).toInt() shl 16 or
            (green * 255.0f + 0.5f).toInt() shl 8 or
            (blue * 255.0f + 0.5f).toInt()
}

fun Int.toHexString(): String {
    return String.format("%08X", this)
}