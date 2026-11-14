package com.pushuuu.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Glass = Color.White.copy(alpha = 0.08f)
private val GlassStrong = Color.White.copy(alpha = 0.12f)
private val GlassBorder = Color.White.copy(alpha = 0.14f)
private const val MAX_PINNED = 5

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen(apps: List<AppItem>, prefs: Prefs, onScreenshot: () -> Unit) {
    val context = LocalContext.current
    val accents = PushuuuThemeAccents.all

    var accentIndex by remember { mutableStateOf(prefs.accentIndex.coerceIn(0, accents.lastIndex)) }
    val accent = accents[accentIndex]
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }
    var showPicker by remember { mutableStateOf(false) }
    val pinned = remember { mutableStateListOf<String>().apply { addAll(prefs.pinned) } }

    BackHandler(enabled = query.isNotEmpty() || category != "All" || showPicker) {
        query = ""
        category = "All"
        showPicker = false
    }

    val categories = remember(apps) {
        listOf("All") + AppCategorizer.ORDER.filter { c -> apps.any { it.category == c } }
    }
    val visible = remember(apps, query, category) {
        apps.filter {
            (category == "All" || it.category == category) &&
                it.name.contains(query.trim(), ignoreCase = true)
        }
    }
    val pinnedApps = pinned.mapNotNull { p -> apps.firstOrNull { it.packageName == p } }

    fun togglePin(pkg: String) {
        if (pinned.contains(pkg)) {
            pinned.remove(pkg)
        } else if (pinned.size < MAX_PINNED) {
            pinned.add(pkg)
        } else {
            Toast.makeText(context, "Dock is full (max $MAX_PINNED)", Toast.LENGTH_SHORT).show()
        }
        prefs.pinned = pinned.toList()
    }

    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(15_000)
        }
    }
    val timeText = remember(now) { SimpleDateFormat("h:mm", Locale.getDefault()).format(now) }
    val dateText = remember(now) { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = accent,
            background = Color.Black,
            surface = Color(0xFF1C1C1E),
            surfaceTint = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x99000000), Color(0xE6000000))))
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 1100f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp)
            ) {
                // Header: clock + date (double-tap = screenshot) and theme button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(onScreenshot) {
                                detectTapGestures(onDoubleTap = { onScreenshot() })
                            }
                    ) {
                        Text(
                            text = timeText,
                            color = Color.White,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-2).sp
                        )
                        Text(
                            text = dateText,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accent)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            .clickable { showPicker = !showPicker }
                    )
                }

                if (showPicker) {
                    LazyRow(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(accents) { i, c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .then(
                                        if (i == accentIndex) Modifier.border(2.dp, Color.White, CircleShape)
                                        else Modifier
                                    )
                                    .clickable {
                                        accentIndex = i
                                        prefs.accentIndex = i
                                    }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Search pill
                val searchShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(searchShape)
                        .background(Glass)
                        .border(1.dp, GlassBorder, searchShape)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    if (query.isEmpty()) {
                        Text("Search apps", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(accent),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = { visible.firstOrNull()?.let { launchApp(context, it) } }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Category chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { c ->
                        val selected = c == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) accent else Glass)
                                .clickable { category = c }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = c,
                                color = if (selected) Color.Black else Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // App grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        top = 18.dp,
                        bottom = if (query.isEmpty()) 140.dp else 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(visible, key = { it.packageName }) { app ->
                        AppCell(
                            app = app,
                            isPinned = pinned.contains(app.packageName),
                            onTogglePin = { togglePin(app.packageName) }
                        )
                    }
                }
            }

            // Glass dock
            if (query.isEmpty()) {
                val dockShape = RoundedCornerShape(30.dp)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .clip(dockShape)
                        .background(GlassStrong)
                        .border(1.dp, GlassBorder, dockShape)
                        .padding(14.dp),
                    horizontalArrangement = if (pinnedApps.isEmpty()) Arrangement.Center else Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pinnedApps.isEmpty()) {
                        Text(
                            text = "Long-press an app to pin it here",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 18.dp)
                        )
                    } else {
                        pinnedApps.forEach { app ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .combinedClickable(
                                        onClick = { launchApp(context, app) },
                                        onLongClick = {
                                            togglePin(app.packageName)
                                            Toast.makeText(context, "Unpinned ${app.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                            ) {
                                AppIcon(app.packageName, 56.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCell(app: AppItem, isPinned: Boolean, onTogglePin: () -> Unit) {
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = { launchApp(context, app) },
                    onLongClick = { menu = true }
                )
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIcon(app.packageName, 58.dp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = app.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text(if (isPinned) "Unpin from dock" else "Pin to dock") },
                onClick = { menu = false; onTogglePin() }
            )
            DropdownMenuItem(
                text = { Text("App info") },
                onClick = {
                    menu = false
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )
            DropdownMenuItem(
                text = { Text("Uninstall") },
                onClick = {
                    menu = false
                    context.startActivity(
                        Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )
        }
    }
}

@Composable
private fun AppIcon(packageName: String, size: Dp) {
    val context = LocalContext.current
    val bitmap = remember(packageName) { IconCache.get(context, packageName) }
    val shape = RoundedCornerShape(size * 0.28f)
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.size(size).clip(shape)
        )
    } else {
        Box(modifier = Modifier.size(size).clip(shape).background(Color.DarkGray))
    }
}

private fun launchApp(context: Context, app: AppItem) {
    try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open ${app.name}", Toast.LENGTH_SHORT).show()
    }
}
