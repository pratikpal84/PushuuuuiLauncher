#!/bin/sh
# One-command build. Needs JDK 17, Android SDK (ANDROID_HOME) and Gradle 8.5 (or a ./gradlew).
if [ -x ./gradlew ]; then ./gradlew assembleDebug; else gradle assembleDebug; fi
echo "APK: app/build/outputs/apk/debug/app-debug.apk"
