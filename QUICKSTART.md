# Quick Start Guide

Get Campalette up and running in minutes!

## Prerequisites

- [ ] Android Studio (latest version recommended)
- [ ] Android device or emulator with camera
- [ ] JDK 17 or higher

## Installation Steps

### 1. Clone the Repository

```bash
git clone https://github.com/vishnu-17o7/campalette.git
cd campalette
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Click "Open" or "File → Open"
3. Navigate to the cloned `campalette` directory
4. Click "OK"

### 3. Wait for Gradle Sync

Android Studio will automatically:
- Download dependencies
- Configure the project
- Index files

This may take 2-5 minutes on first open.

### 4. Run the App

**Option A: Using Toolbar**
1. Select a device/emulator from the dropdown
2. Click the green "Run" button (▶️)

**Option B: Using Menu**
1. Go to "Run → Run 'app'"
2. Select deployment target
3. Click "OK"

**Option C: Command Line**
```bash
./gradlew installDebug
```

## First Use

### 1. Grant Permission
When you first launch the app:
1. You'll see a permission request screen
2. Tap "Grant Permission"
3. In the system dialog, tap "Allow"

### 2. Take a Photo
1. Point your camera at something colorful
2. Tap the "TAKE PHOTO" button
3. Wait for processing (~1 second)

### 3. View Palette
- Extracted colors appear below the camera
- Each card shows:
  - Color swatch
  - Color name
  - Hex code

### 4. Try Different Subjects
Take photos of:
- 🌅 Sunset/sunrise
- 🌸 Flowers
- 🎨 Artwork
- 🍎 Food
- 🏙️ Architecture

## Troubleshooting

### Build Fails

**Error**: "SDK location not found"
```bash
# Create local.properties
echo "sdk.dir=/path/to/Android/sdk" > local.properties
```

**Error**: "Could not resolve dependencies"
- Check internet connection
- Verify you can access Google Maven repository
- See BUILD.md for mirror repository setup

### Camera Not Working

**Emulator**:
1. Edit AVD settings
2. Under "Camera", select "Webcam" for front/back
3. Restart emulator

**Physical Device**:
1. Ensure camera permissions are granted
2. Check if other camera apps work
3. Restart the app

### No Colors Extracted

Some images may not produce all 7 color types:
- This is normal behavior
- Palette API only extracts colors it finds significant
- Try photos with more color variety

## Tips for Best Results

### Good Photo Subjects
✅ High color contrast  
✅ Multiple distinct colors  
✅ Well-lit scenes  
✅ Clear, sharp focus  

### Avoid
❌ Monochrome scenes  
❌ Very dark/dim lighting  
❌ Blurry images  
❌ Overexposed images  

## Understanding the Palette

### Dominant
Most common color in the image

### Vibrant
Bold, saturated color

### Muted
Subdued, less saturated color

### Light/Dark Variants
Lighter or darker versions of vibrant/muted colors

## Customization

### Change Theme
The app automatically:
- Follows system light/dark mode
- Uses dynamic colors (Android 12+)

### Taking Better Photos
- Ensure good lighting
- Keep camera steady
- Focus on colorful subjects
- Avoid motion blur

## Next Steps

After getting familiar with the app:

1. **Read the Docs**
   - [README.md](README.md) - Full documentation
   - [ARCHITECTURE.md](ARCHITECTURE.md) - Technical details
   - [CONTRIBUTING.md](CONTRIBUTING.md) - Contribute to the project

2. **Explore the Code**
   - `MainActivity.kt` - Main application logic
   - `ui/theme/` - Material 3 theme implementation

3. **Try Modifications**
   - Change colors in `Color.kt`
   - Adjust layout proportions in `MainActivity.kt`
   - Add new features!

## Common Use Cases

### Design Inspiration
Extract color palettes from:
- Nature photos
- Artwork
- Fashion
- Interior design

### Development
Use extracted colors for:
- App themes
- Web design
- Branding
- UI design

### Photography
Analyze color composition of:
- Landscape photos
- Portrait photos
- Product photos
- Architectural photos

## Getting Help

- 📖 Check [README.md](README.md) for detailed info
- 🔧 See [BUILD.md](BUILD.md) for build issues
- 💬 Create an issue on GitHub
- 📧 Contact the maintainers

## Share Your Palettes!

Found an interesting color palette? Share it!
- Tag us on social media
- Create an issue with "Show & Tell" label
- Contribute to the project

---

**Ready to extract some colors?** 🎨  
Happy palette hunting! 🚀
