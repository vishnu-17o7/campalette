# Campalette

A Material 3 Android app that extracts colors from images captured through the camera and presents a beautiful color palette.

## Features

- 📸 **Camera Integration**: Real-time camera preview with CameraX
- 🎨 **Color Extraction**: Advanced color palette extraction using AndroidX Palette API
- 🌈 **Material 3 UI**: Modern, beautiful user interface following Material Design 3 guidelines
- 🎭 **Multiple Color Swatches**: Extracts Dominant, Vibrant, Muted, Light, and Dark color variations
- 📱 **Responsive Design**: Optimized for various screen sizes
- 🌙 **Dynamic Theming**: Supports light and dark themes with dynamic colors (Android 12+)

## Screenshots

The app consists of two main sections:
1. **Camera Preview** (60% of screen): Live camera feed with a capture button
2. **Color Palette** (40% of screen): Displays extracted colors with hex codes

## Technical Details

### Architecture
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Camera**: CameraX API
- **Color Extraction**: AndroidX Palette library
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Key Components

#### MainActivity.kt
The main activity implements:
- Camera permission handling
- CameraX integration for camera preview
- Image capture functionality
- Color palette extraction and display
- Material 3 themed UI with Compose

#### Color Extraction
Uses the AndroidX Palette API to extract:
- **Dominant Color**: The most prominent color in the image
- **Vibrant Color**: A saturated, vivid color
- **Muted Color**: A subdued, less saturated color
- **Light Vibrant**: A light, vivid color
- **Dark Vibrant**: A dark, vivid color
- **Light Muted**: A light, subdued color
- **Dark Muted**: A dark, subdued color

#### Material 3 Theme
- Implements full Material 3 color system
- Dynamic color support for Android 12+
- Custom color palette with primary, secondary, and tertiary colors
- Proper dark theme support

### Dependencies
```kotlin
// Material 3
implementation("com.google.android.material:material:1.11.0")

// Jetpack Compose
implementation("androidx.compose.material3:material3")

// CameraX
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// Palette API for color extraction
implementation("androidx.palette:palette-ktx:1.0.0")
```

## How to Build

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 34

### Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/vishnu-17o7/campalette.git
   cd campalette
   ```

2. Open the project in Android Studio

3. Sync Gradle files

4. Build and run on an emulator or physical device:
   ```bash
   ./gradlew assembleDebug
   ```

## Usage

1. **Launch the app**: The app will request camera permission on first launch
2. **Grant permission**: Allow camera access when prompted
3. **Take a photo**: Tap the "TAKE PHOTO" button to capture an image
4. **View palette**: The color palette will automatically appear below the camera preview
5. **Analyze colors**: Each color card shows:
   - Color name (Dominant, Vibrant, etc.)
   - Hex color code
   - Visual color swatch

## Permissions

The app requires the following permissions:
- `CAMERA`: For capturing photos
- `READ_MEDIA_IMAGES`: For accessing captured images (Android 13+)

## Project Structure

```
campalette/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/vishnu/campalette/
│   │       │   ├── MainActivity.kt              # Main activity with camera and UI
│   │       │   └── ui/theme/
│   │       │       ├── Color.kt                 # Material 3 color definitions
│   │       │       ├── Theme.kt                 # App theme configuration
│   │       │       └── Type.kt                  # Typography definitions
│   │       ├── res/
│   │       │   ├── values/
│   │       │   │   ├── strings.xml             # App strings
│   │       │   │   ├── colors.xml              # Color resources
│   │       │   │   └── themes.xml              # Material 3 theme
│   │       │   └── mipmap-*/                   # App icons
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Code Highlights

### Camera Preview with Compose
```kotlin
@Composable
fun CameraPreview(onImageCaptured: (Bitmap) -> Unit) {
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            // Setup CameraX with preview and image capture
            ...
        }
    )
}
```

### Color Palette Extraction
```kotlin
private fun extractColorPalette(bitmap: Bitmap): List<PaletteColor> {
    val palette = Palette.from(bitmap).generate()
    // Extract various color swatches
    palette.dominantSwatch?.let { ... }
    palette.vibrantSwatch?.let { ... }
    ...
}
```

### Material 3 UI Components
```kotlin
@Composable
fun ColorCard(paletteColor: PaletteColor) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Color swatch and details
    }
}
```

## Future Enhancements

- [ ] Save extracted color palettes
- [ ] Share palettes with other apps
- [ ] Copy hex codes to clipboard
- [ ] Gallery image selection
- [ ] Export palette as image
- [ ] Color harmony suggestions
- [ ] Custom color naming

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Author

Created with ❤️ for exploring Material 3 design and color extraction on Android.

