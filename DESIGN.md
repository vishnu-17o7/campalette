# Design

## Visual Identity

Campalette uses a custom "Atelier" theme inspired by artist studios and editorial design. The visual language is warm, tactile, and precise — combining serif typography with a paper-like surface palette and rich, nature-inspired accent colors.

## Color Palette

**Strategy**: Committed — teal primary carries identity, warm brown secondary provides warmth, deep green tertiary adds depth.

### Primary (Teal)
| Token | Hex | Usage |
|---|---|---|
| Primary | `#004743` | Buttons, active states, key actions |
| On Primary | `#FFFFFF` | Text/icons on primary |
| Primary Container | `#1F5F5B` | Secondary actions, containers |
| On Primary Container | `#99D7D1` | Text/icons on container |
| Primary Fixed | `#B0EEE8` | Swatch selection rings, highlights |
| Primary Fixed Dim | `#95D2CC` | Subtle highlights |

### Secondary (Warm Brown)
| Token | Hex | Usage |
|---|---|---|
| Secondary | `#81542E` | Secondary actions, accents |
| On Secondary | `#FFFFFF` | Text/icons on secondary |
| Secondary Container | `#FEC394` | Warm containers, tags |
| On Secondary Container | `#794E29` | Text on warm containers |

### Tertiary (Deep Green)
| Token | Hex | Usage |
|---|---|---|
| Tertiary | `#18481A` | Tertiary actions, nature accent |
| On Tertiary | `#FFFFFF` | Text/icons on tertiary |
| Tertiary Container | `#31602F` | Green containers |
| On Tertiary Container | `#A3D99B` | Text on green containers |

### Surface & Background
| Token | Hex | Usage |
|---|---|---|
| Surface | `#FDF9F4` | App background, sheets |
| Surface Container Lowest | `#FFFFFF` | Elevated cards |
| Surface Container Low | `#F7F3EE` | Subtle containers |
| Surface Container | `#F1EDE8` | Default containers |
| Surface Container High | `#EBE8E3` | Subtle emphasis |
| Surface Container Highest | `#E6E2DD` | Strong emphasis |

### Neutrals
| Token | Hex | Usage |
|---|---|---|
| On Surface | `#1C1C19` | Primary text |
| On Surface Variant | `#454D49` | Secondary text |
| Outline | `#707977` | Borders, dividers |
| Outline Variant | `#C7CFCD` | Subtle borders |

## Typography

**Strategy**: Two-family system with clear role separation.

### Font Families
- **Newsreader** (Variable): Editorial serif for headlines, display, and emphasis
- **Manrope** (Variable): Clean sans-serif for body text, labels, and UI

### Type Scale

| Role | Family | Weight | Size | Line Height | Tracking | Usage |
|---|---|---|---|---|---|---|
| Display Large | Newsreader | Light | 56sp | 60sp | -1.2sp | Hero numbers, palette names |
| Headline Large | Newsreader | Regular | 32sp | 36sp | -0.4sp | Screen titles |
| Headline Medium | Newsreader | Regular | 26sp | 30sp | — | Section headings |
| Headline Small | Newsreader | Regular | 20sp | 24sp | — | Card titles |
| Title Large | Newsreader | Regular | 24sp | 28sp | — | Emphasis text |
| Title Medium | Newsreader | Regular | 20sp | 24sp | — | Subheadings |
| Body Large | Manrope | Regular | 16sp | 24sp | — | Body text, descriptions |
| Body Medium | Manrope | Regular | 14sp | 21sp | — | Secondary body text |
| Label Large | Manrope | SemiBold | 12sp | 16sp | 0.2sp | Buttons, tags |
| Label Medium | Manrope | SemiBold | 11sp | 14sp | 1.4sp | Eyebrow labels |
| Label Small | Manrope | Bold | 10sp | 12sp | 1.8sp | Small tags, metadata |

## Shapes

| Token | Radius | Usage |
|---|---|---|
| Default | 16dp | Small components, buttons |
| Medium | 24dp | Medium cards |
| Large | 32dp | Large cards, panels |
| Extra | 48dp | Bottom sheet, special containers |
| Full | 9999dp | Pills, badges, circular |

## Component Patterns

### GlassPanel
Frosted glass effect container with surface-tint background at 12% opacity. Used for overlays on camera view.

### GradientPrimaryButton
Gradient from primary container to primary, with scale animation on press (0.94f). Uppercase label text.

### AtelierSwatch
Circular color swatch with selection ring animation. Shows hex code below. Scale animation on selection (1.15f).

### PaletteStrip
Horizontal scrolling row of AtelierSwatches with optional trailing "add" action.

### FloatingBottomNav
Pill-shaped bottom navigation with three items (History, Camera, Settings). Camera button becomes capture action on Live screen with breathing pulse animation.

### PaletteCard
Card with swatch strip (5 colors), title, subtitle, and optional action icon. Featured variant has larger swatch area.

### EditorialInputField
Styled text input with animated underline (1.5dp → 2.5dp), italic serif text, and uppercase label tag.

## Animation System

### Spring Constants
- **ExpressiveSpatialSpring**: Damping 0.55, Stiffness 300 — for spatial movements (scale, position)
- **ExpressiveEffectsSpring**: Damping 1.0, Stiffness 400 — for effects (opacity, color, ring width)
- **ExpressiveEffectsColorSpring**: Same as above, typed for Color

### Key Animations
- **Capture button breathing**: Infinite pulse 1.0 → 1.08 scale, 1800ms linear
- **Swatch selection**: Scale 1.0 → 1.15 with spring damping 0.7
- **Button press**: Scale down to 0.92–0.94, quick release
- **Screen transitions**: Horizontal slide + fade with AnimatedContent

## Dynamic Color System

When a photo is captured, the dominant color is extracted and blended into the theme:
- Primary shifts toward captured color (22% blend factor)
- Primary container follows with reduced blend (18.7%)
- Surface tint subtly shifts (6.6%)
- Surface container low gets a hint (0.88%)

Blend factor is adjusted based on seed color luminance:
- Dark seeds: 75% of base factor
- Light seeds: 60% of base factor

## Layout Principles

- **Camera-first**: Live screen is full-bleed camera with overlaid controls
- **Generous padding**: 24dp standard content padding
- **Rhythmic spacing**: 10dp, 14dp, 16dp, 20dp, 24dp spacing scale
- **Full-width cards**: Palette cards and panels span container width
- **Scrolling palettes**: Horizontal LazyRow for swatch strips
