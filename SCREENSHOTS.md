# Campalette Screenshots

Since this is a camera-based app, actual screenshots require running the app on a device or emulator. Below are descriptions of what you would see:

## App Interface

### Main Screen (Camera + Palette View)

```
┌─────────────────────────────────────┐
│  STATUS BAR (Material 3 Primary)    │
├─────────────────────────────────────┤
│                                     │
│                                     │
│        CAMERA PREVIEW               │
│      (Live Camera Feed)             │
│                                     │
│          60% of screen              │
│                                     │
│                                     │
│  ┌────────────────────────────┐    │
│  │      [TAKE PHOTO]          │    │
│  │   (FAB Button - M3 Style)  │    │
│  └────────────────────────────┘    │
├─────────────────────────────────────┤
│   COLOR PALETTE                     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ■ Dominant      #6750A4    │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  ■ Vibrant       #FF5722    │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │  ■ Muted         #78909C    │   │
│  └─────────────────────────────┘   │
│        40% of screen                │
│  (Scrollable LazyColumn)            │
│                                     │
└─────────────────────────────────────┘
```

### Permission Screen (First Launch)

```
┌─────────────────────────────────────┐
│  STATUS BAR                          │
├─────────────────────────────────────┤
│                                     │
│                                     │
│          📷                         │
│                                     │
│   Camera permission is required    │
│   to take photos                   │
│                                     │
│  ┌──────────────────────────┐      │
│  │   [Grant Permission]     │      │
│  │   (Material 3 Button)    │      │
│  └──────────────────────────┘      │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

### Color Card Detail

Each color card in the palette displays:

```
┌────────────────────────────────────┐
│  ┌────┐                            │
│  │    │  Vibrant                   │
│  │ ██ │  #FF5722                   │
│  │    │                            │
│  └────┘                            │
│   56dp     Name & Hex Code         │
│  swatch                            │
└────────────────────────────────────┘
```

## Visual Design Elements

### Material 3 Features

1. **Color System**
   - Primary: #6750A4 (Purple)
   - Secondary: #625B71 (Muted Purple)
   - Tertiary: #7D5260 (Rose)
   - Surface: #FFFBFE (Light mode)
   - Background: #FFFBFE (Light mode)

2. **Typography**
   - Headline: 22sp, Regular
   - Body: 16sp, Regular
   - Label: 14sp, Medium

3. **Shape**
   - Cards: 12dp rounded corners
   - Color swatches: 8dp rounded corners
   - FAB: Fully rounded

4. **Elevation**
   - Cards: 2dp elevation
   - FAB: 6dp elevation (default)

### Color Palette Cards

When you capture an image, you'll see up to 7 color cards:

1. **Dominant** - The most common color
   - Example: Background sky color from a landscape

2. **Vibrant** - Bold, saturated color
   - Example: Bright flower petals

3. **Light Vibrant** - Light, vivid color
   - Example: Pastel colors

4. **Dark Vibrant** - Dark, vivid color
   - Example: Deep ocean blue

5. **Muted** - Subdued color
   - Example: Earthy tones

6. **Light Muted** - Light, subdued color
   - Example: Soft neutrals

7. **Dark Muted** - Dark, subdued color
   - Example: Shadow colors

## Example Use Cases

### Landscape Photo
If you take a photo of a sunset:
- **Dominant**: Orange/Yellow (sky)
- **Vibrant**: Deep Orange (sun)
- **Dark Vibrant**: Deep Blue (evening sky)
- **Muted**: Brown (ground)

### Food Photo
If you take a photo of colorful vegetables:
- **Vibrant**: Bright Red (tomato)
- **Light Vibrant**: Yellow (pepper)
- **Dominant**: Green (leafy greens)
- **Dark Muted**: Brown (wooden table)

### Architecture Photo
If you take a photo of a building:
- **Dominant**: Gray (concrete)
- **Vibrant**: Blue (sky)
- **Dark Muted**: Dark Gray (shadows)
- **Light Muted**: Off-white (walls)

## Interaction Flow

1. **Launch** → Permission request (if needed)
2. **Grant Permission** → Camera preview appears
3. **Point Camera** → See live preview
4. **Tap "TAKE PHOTO"** → Capture image
5. **Processing** → (< 1 second)
6. **View Palette** → Colors appear below camera
7. **Scroll** → See all extracted colors
8. **Take Another** → Repeat process

## Theme Variations

### Light Mode
- White/light backgrounds
- Dark text on light surfaces
- Purple accent colors

### Dark Mode
- Dark backgrounds (#1C1B1F)
- Light text on dark surfaces
- Lighter purple accents

### Dynamic Color (Android 12+)
- Colors adapt to wallpaper
- System-wide color harmony
- Personalized palette

## Accessibility

- High contrast ratios (WCAG AA compliant)
- Minimum 48dp touch targets
- Clear visual hierarchy
- Semantic labels for screen readers

## To Generate Actual Screenshots

Run the app and use:
- **Android Studio**: Tools → Layout Inspector
- **Device**: Screenshot via power + volume down
- **adb**: `adb shell screencap -p /sdcard/screenshot.png`

Then add them to `/screenshots` directory in the repo.
