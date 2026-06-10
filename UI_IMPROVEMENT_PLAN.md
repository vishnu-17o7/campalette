# Campalette UI Improvement Plan

## Project Context Summary

**Campalette** is a Material 3 Android app that extracts color palettes from camera-captured images. It already has a sophisticated custom design system ("Atelier") with:

- **Custom theme**: Teal primary, warm brown secondary, deep green tertiary; paper-like surfaces
- **Typography**: Newsreader (serif) for display/headlines, Manrope (sans) for body/labels
- **Animations**: Spring-based expressive animations, breathing capture button, wavy progress indicator
- **Haptics**: 13 distinct haptic events mapped to interactions
- **Screens**: Live Camera, Color Detail, Palette Library, Palette Editor, Settings, Onboarding Permission
- **Advanced features**: Dynamic color theming (shifts UI based on captured dominant color), color sampling from image (tap/long-press with radial menu), harmony analysis (Analogous/Complementary/Tonal), palette history/saving

---

## Improvement Areas by Priority

### 🔴 High Priority — Core Experience

#### 1. Live Camera Screen — Empty & Waiting States
**Current**: The live camera shows only the preview with a reticle. Before capture, there is no guidance or visual interest.
**Improvement**:
- Add a **pre-capture overlay** with a subtle animated hint: "Tap capture to sample colors from the scene"
- Show a **mini default palette strip** (the 5 Atelier brand colors) as a teaser of what the app produces
- Add a **subtle animated border/frame** around the camera preview that pulses gently, inviting capture
- When camera permission is granted but no capture yet, show a **floating tip card** that auto-dismisses after first capture

#### 2. Capture-to-Palette Transition
**Current**: A wavy progress indicator with "Analyzing Colors..." text appears in a box.
**Improvement**:
- Replace the centered box with a **full-screen immersive transition**: the captured image briefly fills the screen, then "shatters" or dissolves into the color swatches that fly to their positions in the palette strip
- Add a **shimmer sweep** across the captured image before extraction
- Play a **satisfying haptic chord** (the existing `CaptureSuccess` is good, but could be richer)
- Show **individual color swatches appearing one by one** with staggered animation (100ms delay each) rather than all at once

#### 3. Color Detail Screen — Visual Impact
**Current**: Full-screen color background with a bottom sheet showing hex, RGB, CMYK, name, and an "Add to Palette" button.
**Improvement**:
- Add **complementary color accent shapes** in the background (large soft circles in the complementary color, creating a more artistic feel)
- Show a **larger hex code** with the Display Large type scale (56sp) — make it the hero element
- Add **color values in multiple formats** (HEX, RGB, CMYK, HSL, HSV) in a horizontally swipeable row of "color chips"
- Add a **"Color Name" guess** using a simple algorithm or API (e.g., "This color is close to 'Terracotta'")
- Add **copy buttons per format** (not just HEX) with individual haptic feedback
- Add a **"Related Colors" section** showing tints, shades, and tones of the selected color
- Add a **swipe gesture** to navigate to the next/previous color in the palette without going back

#### 4. Palette Editor — Reordering & Interaction
**Current**: Static list of color cards. Users can select a color and inspect it, or analyze harmony.
**Improvement**:
- Add **drag-and-drop reordering** of swatches (using Compose's `detectDragGestures` or `LazyList` reordering)
- Add a **"Delete" action** on swipe (or long-press menu) to remove colors from the palette
- Add a **"Duplicate" action** to copy a color
- Show **live harmony preview**: as you reorder, show a subtle background shift suggesting the new harmony
- Add a **color picker dialog** to manually adjust any swatch (HSV wheel or sliders)
- Add **palette statistics** in a header: average saturation, warmth/coolness indicator, contrast ratio of lightest/darkest

#### 5. Share & Export
**Current**: No sharing or export functionality exists.
**Improvement**:
- Add a **share palette as image** feature: generate a beautiful card (1080x1920) with the palette colors, names, hex codes, and the Campalette brand
- Add **copy-all-hex-codes** button (comma-separated or JSON array)
- Add **export to formats**: CSS variables, Swift UIColor, Android ColorRes, Figma JSON
- Add **share to social** with a branded palette card image

---

### 🟡 Medium Priority — Polish & Depth

#### 6. Onboarding / First-Launch Experience
**Current**: Single permission screen with a gradient background and CTA.
**Improvement**:
- Add a **2-3 page onboarding flow** after permission grant:
  1. "Point your camera at anything" — animated illustration of camera + colors flowing out
  2. "Tap to sample, long-press to explore" — show tap and long-press gestures
  3. "Save and share your studies" — show library and export cards
- Use **Lottie-style animations** (or simple Canvas animations) for each page
- Add a **"Skip" button** for returning users
- Store onboarding completion in preferences

#### 7. Settings Screen — Visual Upgrade
**Current**: Basic LazyColumn with Switch rows in surface containers.
**Improvement**:
- Add **illustrated setting categories** with icons and descriptions
- Add a **theme preview card** at the top showing current primary/secondary/tertiary/surface colors
- Add a **"Reset to Default"** action for the dynamic theme
- Add **accessibility toggles**: high contrast mode, larger touch targets, reduced motion (respect system setting + manual override)
- Add **camera quality setting**: high/medium/low resolution for captures
- Add a **"About / Credits"** section with app version, design system name, and open-source licenses

#### 8. Palette Library — Search, Filter, Organization
**Current**: LazyColumn of PaletteCards with a featured card at top and staggered archive below.
**Improvement**:
- Add a **search bar** at the top to filter by palette name or color hex
- Add **filter chips**: "All", "Camera Captures", "Harmony Studies", "Saved"
- Add **sort options**: Recent first, Name A-Z, Color count, Warmest first, Coolest first
- Add **grid view toggle** (1-column vs 2-column compact cards)
- Add **swipe-to-delete** with undo Snackbar
- Add **palette folders/tags**: let users create custom collections ("Brand Colors", "Inspiration", "Project X")
- Add **favorite/star** action on cards

#### 9. Floating Bottom Navigation — Enhanced Transitions
**Current**: Pill-shaped nav with History, Camera, Settings. Camera button becomes capture action on Live screen.
**Improvement**:
- Add **morphing animation** when the center button switches between "Camera" icon and "Capture" icon (shape transformation)
- Add **badge indicators** on History tab showing number of unsaved captures
- Add a **peek animation** when a new capture is ready (the nav bar briefly bounces)
- Consider adding a **"Quick Actions" long-press menu** on the center button: "Capture", "Import from Gallery", "Create Empty Palette"

#### 10. Radial Menu — Interactivity Upgrade
**Current**: Canvas-drawn ring of colors around the touch point. Static display.
**Improvement**:
- Make the **radial segments tappable** — tapping a segment selects that color and closes the menu
- Add **color name labels** on each segment
- Add a **center preview** showing the exact touched color with hex code
- Add **animation**: segments expand outward from center with staggered spring animation
- Add **gesture**: rotate the ring with finger to cycle through colors

---

### 🟢 Low Priority — Delight & Differentiation

#### 11. Splash / Launch Animation
**Current**: Standard Android cold start.
**Improvement**:
- Add a **branded splash screen** with the Campalette wordmark and a breathing color orb that shifts through the Atelier brand colors
- Use the Android 12+ SplashScreen API with a custom animated vector drawable
- Transition smoothly into the Live camera screen

#### 12. Real-Time Color Preview While Sampling
**Current**: Tap shows a sampled point indicator that fades. Long-press shows a radial menu.
**Improvement**:
- On **drag across the captured image**, show a **magnifier loupe** (circular zoomed view) following the finger with a crosshair
- Show the **live hex code** updating in the loupe
- Add a **color temperature indicator** (warm/cool/neutral) in the loupe
- On lift, offer a quick action: "Use this color", "Add to palette", "Cancel"

#### 13. Animated Theme Transitions
**Current**: Dynamic theme colors update instantly when a new dominant color is extracted.
**Improvement**:
- Animate the **theme color transition** over 600ms using `animateColorAsState` for primary, primaryContainer, surfaceTint
- This makes the UI feel alive and responsive to the captured scene
- Add a **subtle background particle effect** when theme shifts (tiny colored dots drifting)

#### 14. Color Blindness Preview Modes
**Current**: No accessibility preview for color vision deficiencies.
**Improvement**:
- Add a **simulation toggle** in the editor or detail screen: Protanopia, Deuteranopia, Tritanopia
- Show a **split-screen or overlay** of how the palette appears to someone with that condition
- Add a **warning badge** if two colors in a palette are indistinguishable under a common deficiency

#### 15. Gallery Image Picker Integration
**Current**: Only live camera capture is supported.
**Improvement**:
- Add a **gallery picker** accessible from the Live screen ( FAB or bottom nav long-press)
- Show a **preview crop/rotate UI** before extraction
- Support **batch selection**: pick multiple images to generate multiple palettes at once

#### 16. Tablet / Large Screen Optimization
**Current**: Single-column layouts throughout.
**Improvement**:
- On tablets/foldables, use a **two-pane layout**: camera/preview on left, palette editor on right
- Library becomes a **master-detail** view: list on left, selected palette expanded on right
- Settings becomes a **preference screen** with categories on left, details on right

---

## Implementation Roadmap

### Phase 1: Core Polish (1-2 weeks)
1. Empty/waiting states on Live screen
2. Staggered palette appearance animation
3. Enhanced Color Detail screen (swipe navigation, related colors, multi-format copy)
4. Drag-and-drop in Palette Editor
5. Share palette as image + copy-all-hex

### Phase 2: Organization & Scale (1-2 weeks)
6. Onboarding flow
7. Library search, filter, sort, folders
8. Settings visual upgrade + accessibility toggles
9. Gallery image picker

### Phase 3: Delight & Differentiation (1-2 weeks)
10. Splash animation
11. Real-time magnifier loupe
12. Animated theme transitions
13. Color blindness preview
14. Tablet optimization

---

## Design System Consistency Checklist

For every new UI element, verify:
- [ ] Uses Atelier color tokens (not hardcoded colors)
- [ ] Uses correct typography scale (Newsreader for display, Manrope for UI)
- [ ] Uses shape tokens (16dp/24dp/32dp/48dp/9999dp)
- [ ] Has proper accessibility semantics (contentDescription, role, stateDescription)
- [ ] Has haptic feedback mapped to `AtelierHapticEvent`
- [ ] Uses spring animations (ExpressiveSpatialSpring / ExpressiveEffectsSpring)
- [ ] Respects reduced-motion preference
- [ ] Minimum 48dp touch targets
- [ ] Proper dark theme support (test against AtelierDarkColorScheme)

---

## Files to Modify / Create

| File | Action | Description |
|------|--------|-------------|
| `LiveAndDetailScreens.kt` | Modify | Add empty states, staggered animations, swipe nav, related colors |
| `StudioScreens.kt` | Modify | Add drag-drop, color picker, stats, search/filter, folders |
| `AtelierComponents.kt` | Modify | Add morphing nav, magnifier loupe, share card generator |
| `AppShell.kt` | Modify | Add onboarding flow, gallery picker integration |
| `Theme.kt` / `DynamicColorTheme.kt` | Modify | Add animated theme transitions |
| `AtelierHaptics.kt` | Extend | Add new haptic events for drag, share, delete, etc. |
| `AtelierData.kt` | Extend | Add color name lookup, export format generators, palette stats |
| `strings.xml` | Extend | Add all new copy strings |
| `OnboardingScreens.kt` | Create | New 3-page onboarding composables |
| `ShareCardGenerator.kt` | Create | Canvas-based palette image export |
| `ColorBlindnessSimulator.kt` | Create | Color matrix filters for deficiency preview |

---

## Success Metrics

- **Task success rate**: Can a new user capture, inspect, and save a palette in under 30 seconds?
- **Time to palette**: From app launch to saved palette < 15 seconds for returning users
- **Engagement depth**: Average number of colors inspected per capture (target: 2+)
- **Sharing rate**: % of users who export/share a palette (target: 15%+)
- **Accessibility score**: Passes TalkBack navigation and color contrast audits
