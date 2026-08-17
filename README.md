# WakeUp Alarm Application

An advanced Android alarm app built with Kotlin, Jetpack Compose, CameraX, and Google ML Kit Face/Eye Detection.

## Features
- **Exact Alarms**: Reliable scheduling using `AlarmManager.setAlarmClock()`.
- **Lockscreen Display**: Activity turns screen on and displays over keyguard.
- **In-Memory ML Verification**: Front-camera face & eye detection algorithm verifying user wakefulness over configurable duration (10s to 120s).
- **Local Persistence**: Room Database for persistent alarm storage and history logs.

## Building APK via GitHub Actions
Simply push this repository to GitHub! The included GitHub Actions workflow (`.github/workflows/build_apk.yml`) will automatically build the debug `.apk` file for download under the Actions tab.
