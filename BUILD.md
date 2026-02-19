# Building the App

## Important Note about Build Environment

This project requires access to Google's Android Maven repository (dl.google.com) to download Android Gradle Plugin and related dependencies. If you're in an environment where this domain is blocked, you have several options:

### Option 1: Build in Android Studio (Recommended)

1. Install [Android Studio](https://developer.android.com/studio)
2. Clone the repository
3. Open the project in Android Studio
4. Android Studio will automatically download dependencies
5. Click "Build" > "Make Project" or run on device/emulator

### Option 2: Use a Mirror Repository

If dl.google.com is blocked, you can configure a mirror repository by editing `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        // Add mirror repositories here if needed
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Add mirror repositories here if needed
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        google()
        mavenCentral()
    }
}
```

### Option 3: Command Line Build

```bash
# Ensure you have JDK 17+ installed
./gradlew assembleDebug

# Or for release build
./gradlew assembleRelease

# To install on connected device
./gradlew installDebug
```

## System Requirements

- **JDK**: 17 or higher
- **Android SDK**: 34
- **Gradle**: 8.2 (included via wrapper)
- **Operating System**: Windows, macOS, or Linux

## Build Output

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Testing on Device

1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect your device via USB
4. Run: `./gradlew installDebug`
5. The app will install and you can launch it from your device

## Testing on Emulator

1. Create an AVD (Android Virtual Device) in Android Studio
2. Ensure the emulator has camera support enabled
3. Run the app from Android Studio or via `./gradlew installDebug`

## Troubleshooting

### "Could not resolve com.android.tools.build:gradle"
- Check internet connection
- Verify you can access dl.google.com
- Try using a mirror repository (see Option 2 above)

### "SDK location not found"
Create a `local.properties` file in the project root:
```
sdk.dir=/path/to/your/android/sdk
```

### Camera not working in emulator
- Ensure the AVD has "Webcam" configured for camera
- Grant camera permissions when prompted
- Some emulators may have limited camera support

## Build Variants

The app supports two build variants:
- **debug**: For development and testing
- **release**: For production (requires signing configuration)

## Next Steps

After building, refer to [ARCHITECTURE.md](ARCHITECTURE.md) for implementation details.
