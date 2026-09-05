# Campalette UI Improvement Plan (v2)

> **Archived implementation plan — not current-state documentation.** Phases 0–2 were
> materially implemented by 2026-08-23, including dead-screen removal, string extraction,
> adaptive size classes, phone delete/undo and loupe polish, large-screen layouts, modal
> presentation, the corrected bottom dock, full-shape selection states, haptics, RTL, and
> Reduced Motion. Historical “Problems found” and line numbers below describe the pre-pass
> source and must not be used as open-issue evidence. See `HANDOVER-2026-08-23.md` for the
> verified current state and remaining release/publication work.

Supersedes the June plan, which targeted the old "Atelier" serif direction. The app has
since been redesigned to the Apple-inspired system in [DESIGN.md](DESIGN.md)
(commit `006ad08`). This plan is grounded in the current code and covers two goals:

1. **Make the phone UI better** — fix rough edges, dead code, and interaction gaps.
2. **Make the tablet UI real** — today only the Library adapts; everything else stretches.

---

## 1. Current state assessment

What exists today (verified against source):

| Screen | File | State |
|---|---|---|
| Camera / live | `LiveAndDetailScreens.kt` | Full-bleed preview, 44dp chrome, 76dp shutter, capture review card, "Live" sampling strip |
| Color detail | `LiveAndDetailScreens.kt` | 232dp hero, hex + name, Hex/RGB/HSL copy rows, Tints/Shades, action group |
| Library | `StudioScreens.kt` | Search + segmented filter, inset list, swipe-to-delete, **tablet master-detail (only adaptive screen)** |
| Editor | `StudioScreens.kt` | Nav bar, name field, harmony segmented, stats card, swatch rows with long-press drag reorder + swipe delete |
| Settings | `StudioScreens.kt` | Three inset groups + footnotes, iOS-style switches |
| Export sheet | `ShareExportSheet.kt` | Bottom sheet, 0.86 height, full width, 4 export formats + share image |
| Color-vision sheet | `ColorBlindnessPreviewScreen.kt` | Bottom sheet, 0.72 height, full width, 3 deficiency types |
| Navigation | `AtelierComponents.kt` (`BottomBar`) | 252 × 60dp glass island with a draggable selection lens, hidden in Editor/detail/sheets |
| Theme | `theme/` | iOS-style light/dark schemes, Manrope, optional palette accent |

### Problems found

**A. Dead / unwired code**
- `GalleryImportScreen.kt` and `OnboardingFlowScreen.kt` are never called (AppShell uses
  `OnboardingPermissionScreen` from `StudioScreens.kt` and launches `PickVisualMedia` directly).
- `MagnifierLoupe`, `PaletteRow`, `FilterChip` in `AtelierComponents.kt` are never called.
- The "radial menu" regressed: `RadialMenuState` now renders only a 48dp bordered square
  (`LiveAndDetailScreens.kt:400-412`), so long-press sampling has no real affordance —
  while the finished `MagnifierLoupe` sits unused.

**B. Hardcoded strings (localization + consistency)**
- `LiveAndDetailScreens.kt`: ~20 user-visible strings ("Finding colors", "Retake",
  "Edit palette", "Tap to inspect", "Live", "Color values", "Tints", "Shades", "Copy",
  all camera content descriptions, …).
- `ShareExportSheet.kt`: "Export palette", "Save palette", "Copy for".
- `ColorBlindnessPreviewScreen.kt`: sheet labels partially hardcoded.

**C. Destructive actions without safety net**
- Library rows and grid cards: `swipeToDismiss` deletes instantly, no undo, no red reveal
  behind the row (`StudioScreens.kt:247`, `:269`).
- Editor swatch rows: same instant delete (`StudioScreens.kt:621`).
- Violates the spirit of DESIGN.md ("destructive actions use label and color") and risks
  data loss — saved palettes are user-controlled and uncapped.

**D. Label/action mismatch**
- Color detail primary button says "Edit palette" but performs add-to-palette
  (`LiveAndDetailScreens.kt:646` vs `AppShell.kt:655-660`).

**E. Tablet gaps (the big one)**
- `AdaptiveLayout` (600dp boolean, `AtelierComponents.kt:207-214`) is used **only** by
  the Library. Camera, Editor, Settings, Color detail, and both sheets have no adaptation.
- Both sheets are `fillMaxWidth()` + fixed height fraction — full-width slabs on a 10" tablet.
- Editor and Settings columns stretch edge-to-edge on wide screens.
- Camera review card and Live strip stretch full width; bottom paddings (112/200dp) assume
  a phone-sized navigation island.
- Library grid/list toggle is rendered **only on tablet** (`StudioScreens.kt:166`) —
  phones can't use grid view at all.
- Color detail is a full-screen overlay even on tablets.

**F. Small phone-side rough edges**
- Editor "Save" text button has a sub-48dp touch target (`StudioScreens.kt:551-556`).
- Editor drag reorder assumes a fixed `77.dp` row height while rows are `heightIn(min = 76.dp)`
  — fragile if content wraps (`StudioScreens.kt:628-635`).
- Corner radii drift from DESIGN.md: review card 22dp, Live strip 18dp, library group 18dp
  vs the 16dp inset-group token.
- Landscape phone: review card spans full width and collides with chrome.
- Sheets dismiss only via scrim tap / Done — no swipe-down gesture.

---

## 2. Work plan

### Phase 0 — Foundations & hygiene (unblocks everything)

1. **Delete dead code**: remove `OnboardingFlowScreen.kt`, `GalleryImportScreen.kt`,
   `PaletteRow`, `FilterChip` (and confirm no preview references). Keep `MagnifierLoupe` —
   it gets wired in Phase 1.
2. **Extract all hardcoded strings** to `strings.xml` (screens + sheets + content descriptions).
3. **Upgrade `AdaptiveLayout`** from a boolean to a size-class enum:
   `Compact <600dp`, `Medium 600–839dp`, `Expanded ≥840dp`, provided via a
   `LocalWindowSizeClass` CompositionLocal at the `AppShell` root (keep the
   `BoxWithConstraints` approach — it already reacts to split-screen correctly).
4. **Token sweep**: align radii (16dp inset groups, 12–14dp controls, 28dp sheets) and
   spacing with DESIGN.md; no new hardcoded colors.

### Phase 1 — Phone UI improvements

**Camera (`LiveAndDetailScreens.kt`, `CameraPreview.kt`)**
5. Pre-capture guidance: a one-time floating hint ("Tap to sample a color — long-press to
   explore") that auto-dismisses after the first sample; persisted via a simple preference.
6. Wire `MagnifierLoupe` into sampling: drag after long-press shows the loupe with live hex;
   on lift, commit the sampled palette (replaces the dead 48dp box). Remove `RadialMenuState`
   rendering if the loupe covers the flow.
7. Capture review card: staggered swatch entry using the existing `ColorSwatch` `entryDelay`;
   constrain card width (max 480dp, centered) so it also behaves in landscape.

**Library (`StudioScreens.kt`)**
8. Show the grid/list toggle on phones too.
9. Delete with undo: keep swipe-to-dismiss, but add a red reveal background and route deletes
   through a Snackbar with **Undo** (re-insert at original index) instead of instant removal.

**Editor (`StudioScreens.kt`)**
10. Fix "Save" touch target (48dp min, proper padding).
11. Measure real row height for drag reorder instead of the hardcoded 77dp.
12. Same delete-undo treatment as Library for swatch removal.

**Color detail (`LiveAndDetailScreens.kt`)**
13. Fix the "Edit palette" button — label it "Add to palette" (matches actual behavior).
14. Tap the hero hex to copy it.

**Sheets**
15. Add swipe-down-to-dismiss on both sheets (drag handle already exists visually).

### Phase 2 — Tablet / large-screen UI

16. **Camera (Medium/Expanded)**: keep preview full-bleed; move the capture review card to a
    right-anchored side panel (360–400dp) on Expanded, centered max-480dp card on Medium and
    phone-landscape; Live strip gets a max width and stays above the navigation island.
17. **Editor (Medium/Expanded)**: centered 720dp column on Medium; on Expanded a two-pane
    layout — swatch list + controls left (~420dp), inline color detail right (replaces the
    full-screen Color detail overlay when launched from the editor).
18. **Settings (Medium/Expanded)**: centered 680dp column, iOS-style; groups don't stretch.
19. **Library**: keep master-detail; bound the list pane (min 320dp / max 420dp), and upgrade
    the detail pane — swatch grid instead of a LazyRow, stats, and Save/Edit actions
    (it already has the skeleton at `StudioScreens.kt:404-476`).
20. **Sheets → modal cards**: on Medium/Expanded, render Share/Export and Color-vision as
    centered cards (max 560dp wide, content-height, 28dp corners, scrim) instead of
    full-width bottom sheets. Same content composables, different container.
21. **Color detail (Medium/Expanded)**: centered floating card (max 640dp) over a scrim when
    opened from Camera/Library; inline right pane when opened from Editor (see 17).
22. **Navigation island**: keep the centered 252dp glass island and draggable selection
    lens per DESIGN.md; verify it clears two-pane layouts and landscape insets.

### Phase 3 — Delight (optional, after 1–2 land)

23. Animated palette-accent transitions (`animateColorAsState` on the dynamic theme shifts).
24. Branded splash via the SplashScreen API.
25. Tablet/tablet-landscape screenshot pass for `SCREENSHOTS.md` / Play listing.

---

## 3. File-by-file change map

| File | Phases | Changes |
|---|---|---|
| `ui/components/AtelierComponents.kt` | 0, 2 | Size-class enum + `LocalWindowSizeClass`; delete `PaletteRow`/`FilterChip`; radius tokens |
| `ui/AppShell.kt` | 0, 1, 2 | Provide size class; undo-snackbar plumbing; sheet container switch (bottom sheet vs modal card); editor two-pane routing |
| `ui/screens/LiveAndDetailScreens.kt` | 0, 1, 2 | Strings; hint overlay; loupe wiring; review-card layout; detail label fix; adaptive detail |
| `ui/screens/StudioScreens.kt` | 0, 1, 2 | Strings; grid toggle on phone; undo delete; reorder fix; tablet editor/settings/library panes |
| `ui/screens/ShareExportSheet.kt` | 0, 2 | Strings; modal-card container; swipe-down dismiss |
| `ui/screens/ColorBlindnessPreviewScreen.kt` | 0, 2 | Strings; modal-card container; swipe-down dismiss |
| `ui/screens/OnboardingFlowScreen.kt`, `GalleryImportScreen.kt` | 0 | Delete |
| `ui/state/PaletteViewModel.kt` | 1 | Transient "recently deleted" slot for undo; first-run hint preference |
| `data/CampaletteRepository.kt` | 1 | Persist first-run hint flag |
| `res/values/strings.xml` | 0 | All extracted strings |
| Previews in screen files | 2 | Add `widthDp = 840` / `1280` previews per screen |

## 4. Verification

- `./gradlew.bat :app:assembleDebug :app:lintDebug --no-daemon` after each phase.
- Compose previews at 393 / 600 / 840 / 1280 widths for every changed screen, light + dark.
- Manual pass: phone portrait, phone landscape, 10" tablet portrait/landscape,
  split-screen (confirms `BoxWithConstraints` sizing), TalkBack sweep on changed screens.
- Checklist from DESIGN.md: touch targets ≥48dp, selected-state semantics, destructive =
  label + color, sentence case, no pill filters / card-per-row regressions.

## 5. Explicit non-goals

- No new features beyond what the UI work needs (no folders/tags, no new export formats,
  no Lottie onboarding).
- No navigation-library rewrite; the `AnimatedContent` root stays.
- No foldable hinge-awareness beyond what the size-class approach gives for free.

## 6. Suggested execution order

Phase 0 (small PR) → Phase 1 items 5–15 (phone polish PR) → Phase 2 items 16–22
(tablet PR) → Phase 3 as time allows. Each phase ships independently and leaves the app
releasable.
