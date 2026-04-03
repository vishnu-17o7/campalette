# Design System Strategy: The Digital Atelier

## 1. Overview & Creative North Star
This design system is built to transform a utility tool into a high-end editorial experience. Our Creative North Star is **"The Digital Atelier."** 

We are moving away from the "template" look of standard mobile apps. Instead, we treat the screen as a tactile workbench where digital precision meets artisanal craftsmanship. This is achieved through intentional asymmetry—where elements might break the grid or overlap slightly—to create a sense of human touch. By utilizing extreme typographic scales and deep, tonal layering, we ensure the interface feels like a curated gallery rather than a rigid database.

## 2. Color & Surface Philosophy
The palette is rooted in organic, earthy tones that evoke the feeling of a physical color lab—linseed oil, moss, weathered leather, and heavy-stock paper.

### The "No-Line" Rule
To maintain a premium, seamless aesthetic, **this design system prohibits the use of 1px solid borders for sectioning.** Structural boundaries must be defined exclusively through background color shifts. For example, a `surface-container-low` section should sit on a `surface` background to denote a change in context.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers, like stacked sheets of fine paper. 
- **Base Layer:** `surface` (#fdf9f4)
- **Secondary Areas:** `surface-container-low` (#f7f3ee) 
- **Interactive Elements/Cards:** `surface-container-lowest` (#ffffff) to provide a soft, natural "lift."
- **In-Camera Overlays:** Use `surface-tint` (#2a6864) at 10-15% opacity with a `backdrop-filter: blur(20px)` to create a signature glassmorphic effect.

### Signature Textures
To avoid a flat, "out-of-the-box" look, all primary CTAs and Hero backgrounds should utilize a subtle linear gradient: 
- **From:** `primary` (#004743) 
- **To:** `primary_container` (#1f5f5b) 
This 15-degree tilt adds "soul" and visual depth that flat hex codes cannot replicate.

## 3. Typography: Editorial Authority
We pair the intellectual elegance of a high-contrast serif with the functional clarity of a modern geometric sans-serif.

- **Display & Headlines (Newsreader):** Use these for moments of inspiration—color palette names, "The Daily Hue," or large numeric data. The serif represents the "Art" in the lab.
- **Body & Labels (Manrope):** Use these for technical data (HEX codes, RGB values) and instructional text. The sans-serif represents the "Lab" precision.

**The Power Gap:** Create visual interest by contrasting a `display-lg` headline (3.5rem) directly against a `label-md` (0.75rem). This high-contrast scale is the hallmark of premium editorial design.

## 4. Elevation & Depth
Depth in this system is a result of **Tonal Layering**, not structural shadows.

- **The Layering Principle:** Place a `surface-container-lowest` card on a `surface-container-high` background. The slight delta in brightness creates a sophisticated "pop" without the clutter of a drop shadow.
- **Ambient Shadows:** Shadows are reserved only for "floating" elements like FABs or Bottom Sheets. They must be extra-diffused: `box-shadow: 0 12px 32px rgba(28, 28, 25, 0.06)`. The shadow color is a tinted version of `on-surface`, never pure black.
- **The "Ghost Border" Fallback:** If a border is required for accessibility (e.g., in a high-glare camera view), use the `outline-variant` token at **15% opacity**. A 100% opaque border is considered a design failure in this system.

## 5. Components

### Floating Action Buttons (FAB)
- **Shape:** `rounded-xl` (3rem radius). 
- **Color:** `primary` with `on-primary` icons.
- **Interaction:** On press, the element should expand slightly (1.05x scale) to emphasize the tactile, "squishy" nature of the interface.

### The Color Swatch (Circular)
- **Visual:** Use a `full` rounding. 
- **Detail:** Swatches should have a very subtle inner-shadow (`inset 0 2px 4px rgba(0,0,0,0.1)`) to make the color feel "poured" into the container.
- **Interaction:** When selected, the swatch should develop a "Halo" using the `primary-fixed` token.

### Data-Driven Color Cards
- **Structure:** No dividers. Separate the color preview from the metadata (HEX/CMYK) using a `2.75rem (spacing-8)` vertical gap.
- **Surface:** Always use `surface-container-lowest`. 
- **Typography:** Display the HEX code in `headline-sm` (Newsreader) to treat the data as a piece of art.

### Bottom Sheets
- **Rounding:** Top-left and Top-right corners must use `rounded-xl` (3rem).
- **Background:** Implement Glassmorphism. `surface` color at 85% opacity with a high blur. This allows the user to still feel the presence of the camera feed/colors behind the sheet.

### Input Fields
- **Style:** Underlined only, or softly filled. Forbid the "boxed" input look.
- **Active State:** The underline transitions from `outline-variant` to `primary` with a 300ms ease-in-out.

## 6. Do's and Don'ts

### Do
- Use **asymmetric padding**. For example, a wider left margin (spacing-6) and a tighter right margin (spacing-4) can make a gallery feel more dynamic.
- Use **Newsreader** for numbers. The serif numerals add an artisanal, "measured" feel to color data.
- Embrace **whitespace**. If a screen feels crowded, increase the spacing between the headline and the first component to `spacing-12` (4rem).

### Don't
- **Don't use 1px dividers.** Use a `spacing-px` height box with `surface-container-high` color if you absolutely must separate items, but prefer whitespace.
- **Don't use pure grey.** All "neutrals" in this system are warmed by the Paper Tone (#F7F3EE). Pure grey (#808080) will break the "Modern Artisanal" warmth.
- **Don't center-align everything.** Editorial layouts often thrive on strong left-alignment with specific elements (like a FAB) breaking the flow on the right.