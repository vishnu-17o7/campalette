# Build guide

## Requirements

- JDK 17
- Android SDK 36
- Android Studio or the checked-in Gradle wrapper

Set the Android SDK path in `local.properties` when Android Studio has not created it:

```properties
sdk.dir=C\:\\Users\\you\\AppData\\Local\\Android\\Sdk
```

Keep `local.properties` out of version control.

## Debug build

Windows:

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon
```

macOS or Linux:

```bash
./gradlew :app:assembleDebug --no-daemon
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

Install the debug build on a connected device:

```powershell
.\gradlew.bat :app:installDebug
```

## Verification

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug --no-daemon
```

Run instrumentation tests with an emulator or device attached:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --no-daemon
```

## Release bundle

Create the local upload key once with `.\scripts\create-upload-keystore.ps1` (or `./scripts/create-upload-keystore.sh`). That writes gitignored `release/campalette-upload.jks` and `keystore.properties`. Use the guarded task below for anything intended for Play: it fails when the keystore is missing, incomplete, or the generated bundle does not carry a verifiable JAR signature.

```powershell
.\gradlew.bat clean :app:testDebugUnitTest :app:lintRelease :app:signedBundleRelease --no-daemon
```

Output: `app/build/outputs/bundle/release/app-release.aab`

To verify release compilation without an owner key, use `:app:assembleRelease`. That task produces an unsigned APK for local inspection and must not be uploaded.

Release builds target API 36, enable R8, and remove unused resources. Follow [PUBLISHING.md](PUBLISHING.md) before uploading a bundle.

## Continuous integration

`.github/workflows/android.yml` verifies the Gradle wrapper and runs unit tests, release lint, debug compilation, and an R8-enabled release compile on every pull request and push to `main`. Signing credentials are intentionally excluded from pull-request CI.

## Benchmark build

The `benchmark` module contains startup, scroll, and baseline-profile journeys. It needs a connected emulator or physical device:

```powershell
.\gradlew.bat :app:generateBaselineProfile --no-daemon
```

The benchmark variant uses the debug signing key and must not be uploaded to Google Play.

## Troubleshooting

- Dependency downloads require access to Google's Maven repository and Maven Central.
- Run `adb devices` when Gradle cannot find a target device.
- Wait for `adb shell getprop sys.boot_completed` to return `1` before installing on a newly started emulator.
- Check camera permission and the AVD camera source when the preview does not start.
