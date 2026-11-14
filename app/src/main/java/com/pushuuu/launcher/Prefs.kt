package com.pushuuu.launcher

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("pushuuu_prefs", Context.MODE_PRIVATE)

    var accentIndex: Int
        get() = sp.getInt("accent", 0)
        set(value) { sp.edit().putInt("accent", value).apply() }

    var pinned: List<String>
        get() = (sp.getString("pinned", "") ?: "").split(",").filter { it.isNotBlank() }
        set(value) { sp.edit().putString("pinned", value.joinToString(",")).apply() }
}
