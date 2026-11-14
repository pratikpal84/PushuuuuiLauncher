package com.pushuuu.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo

data class AppItem(
    val name: String,
    val packageName: String,
    val activityName: String,
    val category: String
)

object AppCategorizer {
    val ORDER = listOf("Games", "Social", "Media", "Productivity", "System", "Other")

    fun getApps(context: Context): List<AppItem> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { ri ->
                val pkg = ri.activityInfo.packageName
                val name = ri.loadLabel(pm).toString()
                AppItem(name, pkg, ri.activityInfo.name, classify(ri.activityInfo.applicationInfo, pkg, name))
            }
            .sortedBy { it.name.lowercase() }
    }

    private fun classify(info: ApplicationInfo, pkg: String, name: String): String {
        // 1) Trust the category the app declares, when it declares one.
        when (info.category) {
            ApplicationInfo.CATEGORY_GAME -> return "Games"
            ApplicationInfo.CATEGORY_SOCIAL -> return "Social"
            ApplicationInfo.CATEGORY_AUDIO,
            ApplicationInfo.CATEGORY_VIDEO,
            ApplicationInfo.CATEGORY_IMAGE -> return "Media"
            ApplicationInfo.CATEGORY_PRODUCTIVITY,
            ApplicationInfo.CATEGORY_NEWS -> return "Productivity"
        }

        // 2) Otherwise fall back to keywords.
        val text = ("$pkg ${name.lowercase()}").lowercase()
        fun has(vararg keys: String) = keys.any { text.contains(it) }

        return when {
            has(".game", "games", "puzzle", "ludo", "pubg", "minecraft", "roblox") -> "Games"
            has("instagram", "facebook", "whatsapp", "telegram", "discord", "snapchat",
                "twitter", "messenger", "linkedin", "reddit", "signal") -> "Social"
            has("camera", "photo", "gallery", "video", "youtube", "music", "spotify",
                "podcast", "player") -> "Media"
            has("docs", "sheets", "slides", "office", "notes", "calendar", "mail", "drive", "pdf") -> "Productivity"
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0 -> "System"
            has("setting", "clock", "calc", "contacts", "dialer") -> "System"
            else -> "Other"
        }
    }
}
