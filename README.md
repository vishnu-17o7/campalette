# Campalette

Campalette is an Android color tool for designers, artists, developers, and photographers. Capture a scene or choose a photo, extract its palette, then refine and export the result.

## What it does

- Captures palettes with CameraX
- Imports one image through Android's system photo picker
- Samples an exact color from a captured frame
- Builds analogous, complementary, and tonal harmonies
- Reorders, removes, inspects, and names palette colors
- Saves palettes and capture history on the device
- Previews common color-vision differences
- Shares palette images and copies HEX values
- Exports CSS, Swift, Android XML, and Figma JSON
- Supports light, dark, reduced-motion, phone, and tablet layouts

Campalette has no accounts, ads, analytics, or developer-operated backend. Camera frames and selected photos stay on the device unless you choose an app from Android's share sheet. Read the [privacy policy](PRIVACY.md) for details.

## Requirements

- Android Studio with JDK 17
- Android SDK 36
- Android 7.0 or newer on the target device

## Build and test

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-daemon
```

On macOS or Linux:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-daemon
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Release

The app targets API 36. Release builds use R8 code shrinking and resource shrinking. Copy `keystore.properties.example` to `keystore.properties`, add your upload-key values, then run:

```powershell
.\gradlew.bat clean :app:testDebugUnitTest :app:lintRelease :app:signedBundleRelease --no-daemon
```

The Android App Bundle is written to `app/build/outputs/bundle/release/app-release.aab`. Follow [PUBLISHING.md](PUBLISHING.md) (mapped to Android’s prepare-for-release checklist) and [play/console.md](play/console.md) before uploading. Terms of use live in [EULA.md](EULA.md).

## Project layout

```text
app/
  src/main/java/com/vishnu/campalette/
    data/           Local palette and settings persistence
    ui/             App shell, data transforms, and haptics
    ui/components/  Shared Compose components and CameraX preview
    ui/screens/     Camera, library, editor, detail, export, and settings
    ui/state/       Screen state and ViewModels
    ui/theme/       Campalette color, type, shape, and dynamic accent system
benchmark/          Macrobenchmark and baseline-profile journeys
fastlane/metadata/  Google Play listing copy
```

Product behavior lives in [PRODUCT.md](PRODUCT.md). Interface rules live in [DESIGN.md](DESIGN.md).

## License

Campalette is available under the [MIT License](LICENSE). Bundled font software is licensed separately under the SIL Open Font License 1.1; see [Third-party notices](THIRD_PARTY_NOTICES.md).
