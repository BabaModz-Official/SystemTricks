package com.babamodz.darktricks

enum class TweakCategory(val title: String, val icon: String) {
    ALL("All Tweaks", "🌟"),
    HARDWARE("Hardware Keys & Gestures", "🔘"),
    NOTIFICATIONS("Notifications", "🔔"),
    STATUS_BAR("Status Bar & UI", "📶"),
    APPS("App Specific Patches", "🛡️")
}

enum class TweakType {
    SWITCH,
    LIST,
    TEXT
}

sealed class ListItem {
    data class Header(val category: TweakCategory, val count: Int) : ListItem()
    data class Tweak(val def: TweakDef) : ListItem()
}

data class TweakDef(
    val key: String,
    val icon: String,
    val title: String,
    val summary: String,
    val category: TweakCategory,
    val type: TweakType,
    val entries: Array<String>? = null,
    val entryValues: Array<String>? = null,
    val defaultValue: Any
)
