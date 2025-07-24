package com.example.todo91.utils

import java.util.Locale

fun String.capitalizeWords(): String = this.replace("_", " ").lowercase(Locale.ROOT)
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }