# Architecture & Implementation Details

## Overview

Campalette is built using modern Android development practices with Jetpack Compose, following the Material 3 design system.

## Technology Stack

### Core Technologies
- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern declarative UI framework
- **Material 3**: Latest Material Design guidelines
- **CameraX**: Modern camera API
- **AndroidX Palette**: Color extraction library

### Architecture Pattern
- **Single Activity**: Uses a single `MainActivity` with Compose
- **Unidirectional Data Flow**: State flows down, events flow up
- **Compose State Management**: Uses `remember` and `mutableStateOf`

## Component Breakdown

### 1. MainActivity.kt

The main entry point that orchestrates the entire app.

#### Key Responsibilities:
- **Permission Management**: Handles camera permission requests
- **Camera Lifecycle**: Manages CameraX lifecycle with lifecycle owner
- **State Management**: Maintains UI state for captured images and color palettes
- **UI Composition**: Composes all UI elements

#### Important Functions:

**CameraScreen()**
```kotlin
@Composable
fun CameraScreen() {
    // Manages overall screen layout
    // 60% camera preview, 40% color palette
    Column {
        Box(weight = 0.6f) { CameraPreview(...) }
        Box(weight = 0.6f) { ColorPaletteDisplay(...) }
    }
}
```

**CameraPreview()**
```kotlin
@Composable
fun CameraPreview(onImageCaptured: (Bitmap) -> Unit) {
    // Uses AndroidView to embed native Preview
    // Configures CameraX with:
    // - Preview use case
    // - ImageCapture use case
    // - Back camera selector
}
```

**extractColorPalette()**
```kotlin
private fun extractColorPalette(bitmap: Bitmap): List<PaletteColor> {
    // Uses Palette.Builder
    // Extracts 7 types of color swatches
    // Returns list of PaletteColor data class
}
```

### 2. UI Theme System

Located in `ui/theme/` package:

#### Color.kt
- Defines Material 3 color tokens
- Separate light and dark theme colors
- Uses proper M3 color roles (primary, secondary, tertiary, etc.)

#### Theme.kt
- Composes the complete Material 3 theme
- Implements dynamic color for Android 12+
- Falls back to static colors for older versions
- Manages status bar appearance

#### Type.kt
- Defines typography scale
- Uses Material 3 type tokens
- Configures font families and styles

## Data Flow

```
User Action (Take Photo)
    ↓
CameraPreview captures image
    ↓
ImageProxy → Bitmap conversion
    ↓
extractColorPalette(bitmap)
    ↓
Palette.Builder.generate()
    ↓
Extract color swatches
    ↓
Update colorPalette state
    ↓
ColorPaletteDisplay recomposes
    ↓
Show color cards to user
```

## CameraX Integration

### Use Cases
1. **Preview**: Live camera feed
   ```kotlin
   Preview.Builder().build().also {
       it.setSurfaceProvider(previewView.surfaceProvider)
   }
   ```

2. **ImageCapture**: Taking photos
   ```kotlin
   ImageCapture.Builder()
       .setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY)
       .build()
   ```

### Image Processing Pipeline
```
ImageProxy (YUV format)
    ↓
Extract first plane buffer
    ↓
BitmapFactory.decodeByteArray()
    ↓
Apply rotation correction
    ↓
Return RGBA Bitmap
```

## Color Palette Extraction

### Palette API Usage
```kotlin
val palette = Palette.from(bitmap).generate()
```

### Extracted Swatches
1. **Dominant**: Most common color
2. **Vibrant**: Saturated, bold color
3. **Muted**: Subdued color
4. **Light Vibrant**: Light, vivid color
5. **Dark Vibrant**: Dark, vivid color
6. **Light Muted**: Light, subdued color
7. **Dark Muted**: Dark, subdued color

### Swatch Properties
Each swatch provides:
- `rgb`: Color as integer
- `population`: Number of pixels
- `titleTextColor`: Contrasting text color
- `bodyTextColor`: Body text color

## Material 3 Implementation

### Color Roles
- **Primary**: Main brand color (#6750A4)
- **Secondary**: Accent color (#625B71)
- **Tertiary**: Additional accent (#7D5260)
- **Error**: Error states (#B3261E)
- **Background/Surface**: Canvas colors

### Dynamic Color (Android 12+)
```kotlin
when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        if (darkTheme) dynamicDarkColorScheme(context) 
        else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}
```

### Component Elevation
```kotlin
Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
)
```

### Shape System
```kotlin
RoundedCornerShape(12.dp)  // Cards
RoundedCornerShape(8.dp)   // Color swatches
```

## State Management

### Composition Local State
```kotlin
var hasCameraPermission by remember { mutableStateOf(...) }
var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
var colorPalette by remember { mutableStateOf<List<PaletteColor>>(emptyList()) }
```

### State Updates
- Permission state updates trigger permission UI
- Image capture updates palette extraction
- Palette update triggers UI recomposition

## Permission Handling

### Runtime Permissions
```kotlin
private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted: Boolean ->
    if (isGranted) recreate()
}
```

### Permission Check
```kotlin
ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.CAMERA
) == PackageManager.PERMISSION_GRANTED
```

## Threading

### CameraX Executor
```kotlin
private lateinit var cameraExecutor: ExecutorService

override fun onCreate(savedInstanceState: Bundle?) {
    cameraExecutor = Executors.newSingleThreadExecutor()
}

override fun onDestroy() {
    cameraExecutor.shutdown()
}
```

### Image Capture Callback
Runs on background thread, safe for processing:
```kotlin
capture.takePicture(
    cameraExecutor,
    object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            // Runs on executor thread
        }
    }
)
```

## UI Layout Structure

```
MainActivity
└── CampaletteTheme
    └── Surface
        └── CameraScreen
            ├── Column (fillMaxSize)
            │   ├── Box (weight 0.6) - Camera
            │   │   └── CameraPreview
            │   │       ├── AndroidView (PreviewView)
            │   │       └── FloatingActionButton
            │   └── Box (weight 0.4) - Palette
            │       └── ColorPaletteDisplay
            │           └── LazyColumn
            │               └── ColorCard (for each color)
```

## Performance Considerations

1. **Lazy Loading**: Uses LazyColumn for color list
2. **Efficient Recomposition**: Minimal state dependencies
3. **Background Processing**: Image processing on executor thread
4. **Image Rotation**: Handled efficiently with Matrix
5. **Memory Management**: ImageProxy.close() after processing

## Security

1. **Permission Requests**: Proper runtime permission handling
2. **No External Storage**: Works with in-memory bitmaps
3. **No Network Access**: Fully offline app
4. **No Data Collection**: No analytics or tracking

## Accessibility

1. **Material 3 Components**: Built-in accessibility support
2. **High Contrast**: Proper color contrast ratios
3. **Touch Targets**: Minimum 48dp touch targets
4. **Screen Reader**: Semantic content descriptions

## Testing Recommendations

### Unit Tests
- Color extraction logic
- Bitmap conversion
- Hex code formatting

### UI Tests
- Permission flow
- Camera preview appearance
- Capture button interaction
- Color palette display

### Integration Tests
- End-to-end capture flow
- Theme switching
- State persistence

## Future Enhancements

### Planned Features
1. **Palette Export**: Save as image or share
2. **Gallery Picker**: Select existing images
3. **Color Clipboard**: Copy hex codes
4. **Custom Names**: Edit color names
5. **Palette History**: Save favorite palettes
6. **ML Integration**: Smart color naming with ML
7. **Accessibility**: Enhanced accessibility features

### Technical Debt
1. Add repository pattern for future data persistence
2. Implement ViewModel for better state management
3. Add dependency injection (Hilt/Koin)
4. Create separate modules for features
5. Add comprehensive test coverage

## Resources

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
- [CameraX](https://developer.android.com/training/camerax)
- [Palette API](https://developer.android.com/training/material/palette-colors)
