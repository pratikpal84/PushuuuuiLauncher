package com.pushuuu.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    private var apps by mutableStateOf(emptyList<AppItem>())
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        prefs = Prefs(this)

        setContent {
            LauncherScreen(
                apps = apps,
                prefs = prefs,
                onScreenshot = { requestScreenshot() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshApps() // picks up newly installed / removed apps
    }

    private fun refreshApps() {
        val ctx = applicationContext
        Thread {
            val list = AppCategorizer.getApps(ctx)
            runOnUiThread { apps = list }
        }.start()
    }

    private fun requestScreenshot() {
        val service = ScreenshotGestureService.instance
        if (service == null) {
            Toast.makeText(this, "Turn on Pushuuu Screenshot in Accessibility settings", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } else if (!service.captureScreen()) {
            Toast.makeText(this, "Screenshots need Android 9 or newer", Toast.LENGTH_SHORT).show()
        }
    }
}
