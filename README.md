# Pushuuu Launcher

Modern Compose home-screen launcher: live clock, glass search bar, category chips,
accent-colour picker (22 colours), pinnable dock, app info/uninstall menu, wallpaper-through look.

## Build the APK (pick one)

**A. Android Studio (easiest on a PC):** File > Open this folder, wait for sync, press the green Run button
(or Build > Build APK(s)). Output: `app/build/outputs/apk/debug/app-debug.apk`.

**B. GitHub, no setup:** push this folder to a GitHub repo > Actions tab > "Build APK" > Run workflow.
Download `PushuuuLauncher-APK` from the finished run.

**C. Command line:** `./build.sh` (Windows: `build.bat`) with JDK 17 + Android SDK + Gradle 8.5.

## Use
Install the APK, press Home, choose "Pushuuu Launcher" > Always.
Double-tap the clock = screenshot (enable "Pushuuu Screenshot" in Accessibility settings once; Android 9+).
Long-press an app = pin / info / uninstall. Tap the coloured dot (top right) = change accent.
