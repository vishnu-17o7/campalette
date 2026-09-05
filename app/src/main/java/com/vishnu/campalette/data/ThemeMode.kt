package com.vishnu.campalette.data

enum class ThemeMode(val storageValue: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    fun resolve(systemInDarkTheme: Boolean): Boolean = when (this) {
        System -> systemInDarkTheme
        Light -> false
        Dark -> true
    }

    companion object {
        fun fromStorageValue(value: String?): ThemeMode = entries.firstOrNull {
            it.storageValue.equals(value, ignoreCase = true)
        } ?: System
    }
}
