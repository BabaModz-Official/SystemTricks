package com.babamodz.darktricks

import android.content.Context

object TweakRepository {

    fun getAllTweaks(context: Context): List<TweakDef> {
        val res = context.resources

        val cursorEntries = res.getStringArray(R.array.cursorControl_entries)
        val cursorValues = res.getStringArray(R.array.cursorControl_values)

        val quickPulldownEntries = res.getStringArray(R.array.quickPulldown_entries)
        val quickPulldownValues = res.getStringArray(R.array.quickPulldown_values)

        val torchTimeoutEntries = res.getStringArray(R.array.torchTimeout_entries)
        val torchTimeoutValues = res.getStringArray(R.array.torchTimeout_values)

        val gestureHeightEntries = res.getStringArray(R.array.gestureHeight_entries)
        val gestureHeightValues = res.getStringArray(R.array.gestureHeight_values)

        val lessNotificationsEntries = res.getStringArray(R.array.lessNotifications_entries)
        val lessNotificationsValues = res.getStringArray(R.array.lessNotifications_values)

        return listOf(
            // Hardware Keys & Gestures
            TweakDef(
                key = "trick_cursorControl",
                icon = "⌨️",
                title = res.getString(R.string.cursorControl_title),
                summary = res.getString(R.string.cursorControl_summary),
                category = TweakCategory.HARDWARE,
                type = TweakType.LIST,
                entries = cursorEntries,
                entryValues = cursorValues,
                defaultValue = "0"
            ),
            TweakDef(
                key = "trick_skipTrack",
                icon = "🎵",
                title = res.getString(R.string.skipTrack_title),
                summary = res.getString(R.string.skipTrack_summary),
                category = TweakCategory.HARDWARE,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_powerTorch",
                icon = "🔦",
                title = res.getString(R.string.powerTorch_title),
                summary = res.getString(R.string.powerTorch_summary),
                category = TweakCategory.HARDWARE,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_torchTimeout",
                icon = "⏱️",
                title = res.getString(R.string.torchTimeout_title),
                summary = res.getString(R.string.torchTimeout_summary),
                category = TweakCategory.HARDWARE,
                type = TweakType.LIST,
                entries = torchTimeoutEntries,
                entryValues = torchTimeoutValues,
                defaultValue = "0"
            ),
            TweakDef(
                key = "trick_proximityWakeUp",
                icon = "🛡️",
                title = res.getString(R.string.proximityWakeUp_title),
                summary = res.getString(R.string.proximityWakeUp_summary),
                category = TweakCategory.HARDWARE,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_doubleTapToSleep",
                icon = "💤",
                title = res.getString(R.string.doubleTapToSleep_title),
                summary = res.getString(R.string.doubleTapToSleep_summary),
                category = TweakCategory.HARDWARE,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_quickPulldown",
                icon = "📱",
                title = res.getString(R.string.quickPulldown_title),
                summary = res.getString(R.string.quickPulldown_summary),
                category = TweakCategory.HARDWARE,
                type = TweakType.LIST,
                entries = quickPulldownEntries,
                entryValues = quickPulldownValues,
                defaultValue = "0"
            ),
            TweakDef(
                key = "trick_gestureHeight",
                icon = "📐",
                title = res.getString(R.string.gestureHeight_title),
                summary = res.getString(R.string.gestureHeight_summary),
                category = TweakCategory.HARDWARE,
                type = TweakType.LIST,
                entries = gestureHeightEntries,
                entryValues = gestureHeightValues,
                defaultValue = "0"
            ),

            // Notifications
            TweakDef(
                key = "trick_lessNotifications",
                icon = "🔕",
                title = res.getString(R.string.lessNotifications_title),
                summary = res.getString(R.string.lessNotifications_summary),
                category = TweakCategory.NOTIFICATIONS,
                type = TweakType.LIST,
                entries = lessNotificationsEntries,
                entryValues = lessNotificationsValues,
                defaultValue = "0"
            ),
            TweakDef(
                key = "trick_screenOffNotifications",
                icon = "📳",
                title = res.getString(R.string.screenOffNotifications_title),
                summary = res.getString(R.string.screenOffNotifications_summary),
                category = TweakCategory.NOTIFICATIONS,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_hideAdbNotification",
                icon = "🔌",
                title = res.getString(R.string.hideAdbNotification_title),
                summary = res.getString(R.string.hideAdbNotification_summary),
                category = TweakCategory.NOTIFICATIONS,
                type = TweakType.SWITCH,
                defaultValue = false
            ),

            // Status Bar & UI
            TweakDef(
                key = "trick_hideVpn",
                icon = "🔑",
                title = res.getString(R.string.hideVpn_title),
                summary = res.getString(R.string.hideVpn_summary),
                category = TweakCategory.STATUS_BAR,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_hideCert",
                icon = "📜",
                title = res.getString(R.string.hideCert_title),
                summary = res.getString(R.string.hideCert_summary),
                category = TweakCategory.STATUS_BAR,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_hideNextAlarm",
                icon = "⏰",
                title = res.getString(R.string.hideNextAlarm_title),
                summary = res.getString(R.string.hideNextAlarm_summary),
                category = TweakCategory.STATUS_BAR,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_show4gForLte",
                icon = "📶",
                title = res.getString(R.string.show4gForLte_title),
                summary = res.getString(R.string.show4gForLte_summary),
                category = TweakCategory.STATUS_BAR,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_hideLtePlus",
                icon = "➕",
                title = res.getString(R.string.hideLtePlus_title),
                summary = res.getString(R.string.hideLtePlus_summary),
                category = TweakCategory.STATUS_BAR,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_hideBuildVersion",
                icon = "🏷️",
                title = res.getString(R.string.hideBuildVersion_title),
                summary = res.getString(R.string.hideBuildVersion_summary),
                category = TweakCategory.STATUS_BAR,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_customCarrierText",
                icon = "🔤",
                title = res.getString(R.string.customCarrierText_title),
                summary = res.getString(R.string.customCarrierText_summary),
                category = TweakCategory.STATUS_BAR,
                type = TweakType.TEXT,
                defaultValue = ""
            ),

            // App Patches
            TweakDef(
                key = "trick_PhoneRecorder",
                icon = "📞",
                title = res.getString(R.string.phoneRecorder_title),
                summary = res.getString(R.string.phoneRecorder_summary),
                category = TweakCategory.APPS,
                type = TweakType.SWITCH,
                defaultValue = false
            ),
            TweakDef(
                key = "trick_OutlookPolicy",
                icon = "💼",
                title = res.getString(R.string.outlookPolicy_title),
                summary = res.getString(R.string.outlookPolicy_summary),
                category = TweakCategory.APPS,
                type = TweakType.SWITCH,
                defaultValue = false
            )
        )
    }
}
