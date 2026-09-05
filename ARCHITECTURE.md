# Campalette architecture

Campalette is a single-activity, offline-first Android app built with Kotlin, Jetpack Compose, CameraX, and AndroidX Palette.

## Runtime structure

```text
MainActivity
  CampaletteTheme
    AppShell
      PaletteViewModel
        CampaletteRepository
      SettingsViewModel
        CampaletteRepository
      Camera, library, editor, detail, settings, and export composables
```

`MainActivity` owns the camera executor and hosts Compose. `AppShell` coordinates root navigation, overlays, camera permission, gallery import, bitmap ownership, and transient feedback.

The app has three root destinations:

- Library
- Camera
- Settings

The editor is a pushed workflow. Color detail, color-vision preview, and export preserve the current workspace as overlays.

## State

`PaletteViewModel` owns:

- The current palette, name, source, selected color, and harmony mode
- Saved palettes and capture history
- Library search, filter, and layout controls
- Persistence ordering for palette mutations

The ViewModel stores the active workspace and library controls in `SavedStateHandle`. A configuration change can restore palette work without retaining the source bitmap.

`SettingsViewModel` owns appearance, palette accent, reduced motion, and haptic preferences. It applies changes optimistically and serializes disk writes.

## Persistence

`CampaletteRepository` is the single disk boundary. It stores compact JSON and settings in the private `campalette` SharedPreferences file.

- Disk reads and writes run off the main thread.
- A mutex preserves write ordering.
- Capture history keeps the newest 100 unique entries.
- Saved palettes remain user-controlled and are not capped.
- Android backup and device transfer include only this preferences file.

The app does not persist camera frames or imported photos.

## Camera and image import

`CameraPreview` binds Preview and ImageCapture use cases to the current lifecycle. It exposes a downscaled reusable sample buffer for tap and long-press sampling.

Capture flow:

```text
CameraX ImageProxy
  -> Bitmap conversion and rotation
  -> Downscale to 1080 px maximum dimension
  -> AndroidX Palette extraction
  -> Palette workspace and capture history
```

Gallery import uses `PickVisualMedia`. Android grants access only to the selected image and falls back to the system document picker on older devices.

Bitmap work runs outside the main thread. `AppShell` owns the displayed bitmap and recycles it when the capture changes or the composable leaves composition.

## Sharing and export

Palette image generation runs on a background dispatcher. The app writes the PNG to `cache/shared_images`, exposes that one file through a non-exported `FileProvider`, and grants temporary read access to the selected share target.

Text export supports:

- HEX lists
- CSS custom properties
- Swift `UIColor`
- Android color resources
- Valid JSON for Figma-oriented workflows

## UI and accessibility

The Compose theme defines light and dark color schemes, Manrope typography, shapes, and an optional palette-derived accent. `LocalReducedMotion` lets transitions replace spatial motion with immediate fades.

Shared components provide:

- Minimum touch targets
- Selected-state semantics
- Explicit descriptions for action icons
- Adaptive phone and tablet layouts
- Camera chrome with controlled contrast over live content

Product behavior and visual rules live in [PRODUCT.md](PRODUCT.md) and [DESIGN.md](DESIGN.md).

## Build and performance

The app compiles against and targets API 36 with a minimum SDK of 24. Release builds use R8 and resource shrinking.

The `benchmark` module covers cold startup, library scrolling, and baseline-profile journeys. The app build also includes the AndroidX profile installer.

Run the standard verification set:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --no-daemon
.\gradlew.bat :app:connectedDebugAndroidTest --no-daemon
.\gradlew.bat :app:lintRelease :app:signedBundleRelease --no-daemon
```

See [BUILD.md](BUILD.md) and [PUBLISHING.md](PUBLISHING.md) for environment and release details.
