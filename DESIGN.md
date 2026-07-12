# Design

## Direction

Campalette uses a native, Apple-inspired interaction model adapted to Android and Jetpack Compose. The interface is quiet, direct, and content-led: the camera and captured colors provide the visual character while navigation and controls stay restrained.

The product has three root destinations: Library, Camera, and Settings. Editing is a pushed workflow, not a fourth tab. Color vision and export are modal sheets that preserve context.

## Core Principles

- Use one clear hierarchy: large root title, grouped content, then actions.
- Reserve glass for floating navigation chrome; ordinary content uses opaque grouped surfaces.
- Keep the camera edge-to-edge and visually dominant.
- Use system-blue for navigation and primary actions, system-green for enabled switches, and system-red for destructive actions.
- Prefer sentence case, short labels, and familiar platform patterns.
- Avoid serif or italic type, decorative gradients, floating tags, pill filters, and card-per-row layouts.

## Color

### Light

| Token | Value | Usage |
|---|---:|---|
| Background | `#F2F2F7` | Grouped screen background |
| Group surface | `#FFFFFF` | Inset lists, sheets, editor rows |
| Secondary fill | `#E5E5EA` | Search, segmented controls, disabled tracks |
| Label | `#000000` | Primary text |
| Secondary label | `#5F5F65` | Metadata and supporting copy |
| Separator | `#C6C6C8` | Inset dividers |
| Accent | `#007AFF` | Selection, navigation, primary actions |
| Success | `#34C759` | Enabled switches |
| Destructive | `#FF3B30` | Remove and destructive actions |

### Dark

| Token | Value | Usage |
|---|---:|---|
| Background | `#000000` | Root background |
| Group surface | `#1C1C1E` | Inset groups and sheets |
| Elevated surface | `#2C2C2E` | Secondary containers |
| Label | `#F5F5F7` | Primary text |
| Secondary label | `#B7B7BC` | Metadata |
| Accent | `#0A84FF` | Dark-mode selection and actions |

Captured palettes may subtly tint selected controls when Palette accent is enabled. The grouped background, body text, destructive color, and switch color remain stable.

## Typography

Manrope is the only product typeface. Its variable `wght` axis must be set explicitly because the bundled font defaults to ExtraLight.

| Role | Weight | Size / line | Usage |
|---|---:|---:|---|
| Large title | 700 | 34 / 40sp | Library and Settings roots |
| Headline | 700 | 24–28 / 29–34sp | Empty states and detail emphasis |
| Navigation title | 600 | 16 / 21sp | Pushed editor and sheet headers |
| Body | 400 | 17 / 24sp | Rows and primary copy |
| Secondary body | 400 | 15 / 21sp | Metadata and descriptions |
| Labels | 500–600 | 11–15sp | Tabs, controls, section labels |

Use tabular-looking hex values with bold sans-serif styling. Do not use all-caps eyebrow labels, italics, or editorial serif display type.

## Shape and Spacing

| Element | Radius / size |
|---|---:|
| Small controls | 10–12dp |
| Search and buttons | 12–14dp |
| Inset groups | 16dp |
| Sheets | 28dp top corners |
| Floating dock | 32dp capsule |
| Dock height | 64dp |
| Camera shutter | 76dp outer / 60dp inner |

Use 16–20dp screen insets, 8–12dp between related controls, and 20–28dp between sections. Separators begin after leading content rather than spanning through icons or swatches.

## Navigation

### Floating Glass Dock

- Three equal destinations: Library, Camera, Settings.
- Maximum width 350dp with 16dp side insets.
- 30dp live blur, subtle noise, a hairline highlight, and no more than 3dp shadow.
- Camera uses dark translucent glass with white inactive items; grouped screens use light glass with dark items.
- The selected item receives a compact translucent lens and accent-colored icon/label.
- The dock is navigation only. Capture is never embedded in it.
- Hide the dock during Editor, color detail, and export/detail overlays.

### Transitions

- Root destinations crossfade in 120–150ms.
- Editor pushes horizontally with a short fade.
- Modal tools rise as bottom sheets over a dimmed context.
- Reduce motion replaces spatial transitions with immediate fades.

## Screen Patterns

### Camera

- Full-bleed preview under transparent system bars.
- 44dp visible circular chrome inside 48dp touch targets.
- A separate 76dp shutter sits above the dock and scales briefly on press.
- Captured results show one compact review surface with swatches, Retake, and Edit palette.
- Active palette sampling uses a small translucent strip, never a second navigation bar.

### Library

- 34sp large title.
- One native search field and one segmented filter.
- Palettes live in a single inset group with swatch preview, title, metadata, separator, and disclosure indicator.
- Empty state directs the user to Camera without adding decorative cards.

### Editor

- Pushed navigation bar with Back, Share, and Save.
- Name field, one harmony segmented control, compact palette statistics, and one inset swatch group.
- Tapping a swatch selects the harmony seed; the selected row uses a checkmark and subtle accent fill.
- Inspecting a color opens detail. Long press supports reordering; removal remains destructive.

### Settings

- Inset grouped rows with 51 × 31dp switches.
- Explanatory copy sits as a footnote beneath the group instead of inflating every row.
- No one-card-per-setting treatment.

### Detail and Sheets

- Color detail uses a 232dp color hero, 34sp bold hex value, grouped copy rows, sentence-case Tints/Shades, and a red remove action.
- Color vision and export are bottom sheets with a scrim, 28dp top corners, drag handle, centered title, and blue Done action.
- Export formats and actions use grouped rows with inset separators.

## Logo

The Campalette mark is a C-shaped camera lens with a white focus point and three small captured-color samples. The primary treatment is system-blue and white with yellow, red, and green optical details. It contains no text, remains inside the adaptive-icon safe zone, and has a monochrome themed-icon variant.

## Accessibility

- Interactive targets are at least 44–48dp.
- Dock and segmented controls expose selected state and tab/radio semantics.
- Icons that duplicate a parent label are decorative; action icons have explicit descriptions.
- Text and controls must retain contrast over arbitrary camera scenes.
- Destructive actions use both label and color, never color alone.
