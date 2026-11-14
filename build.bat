@echo off
if exist gradlew.bat (call gradlew.bat assembleDebug) else (gradle assembleDebug)
echo APK: app\build\outputs\apk\debug\app-debug.apk
