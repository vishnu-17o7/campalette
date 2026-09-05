# Publish Campalette

This follows Android’s [Publish your app](https://developer.android.com/studio/publish) and [Prepare for release](https://developer.android.com/studio/publish/preparing) checklists. Google Play requires an **Android App Bundle** (`.aab`), not an APK, for new apps.

Play Console form answers: [play/console.md](play/console.md).

## Official prepare-for-release status

| Task | Status |
|---|---|
| Application ID stable (`com.vishnu.campalette`) | Done — cannot change after first upload |
| Version `1.0.0` / `versionCode` 1 | Done |
| `isDebuggable = false` on release | Done |
| R8 shrinking + resource shrinking | Done |
| Verbose `Log.d` / `Log.v` stripped in release | Done |
| No WebView, no method tracing, no debug servers | Done |
| Only `CAMERA` and `VIBRATE` permissions | Done |
| Launcher icon + 512 Play icon | Done |
| Feature graphic 1024×500 | Done |
| Terms of use (EULA) | Done — Settings and [EULA.md](EULA.md) |
| Privacy policy | Done — Settings and [PRIVACY.md](PRIVACY.md) |
| No remote servers to stand up | Done — offline-first |
| Upload key valid after 22 Oct 2033 | Script uses 10,000-day validity |
| Signed release App Bundle | Owner must create the upload key locally |
| Test signed build on a phone and a tablet | Owner |
| Promotional screenshots matching the shipped UI | Drafts exist; retake from the signed build |

## 1. Gather materials

- **Upload key:** `.\scripts\create-upload-keystore.ps1` writes gitignored `release/campalette-upload.jks` and `keystore.properties`. Back them up. Play App Signing still needs this certificate on every upload.
- **Icon:** mipmap launcher + `fastlane/metadata/android/en-US/images/icon.png`.
- **EULA / privacy:** [EULA.md](EULA.md), [PRIVACY.md](PRIVACY.md). Both must be on public `main` before Play listing URLs work.
- **Listing copy and art:** `fastlane/metadata/android/en-US/`.

There is no production server, API key, or paid-app license server.

## 2. Configure (already in the Gradle release type)

Release builds set `isDebuggable = false`, minify, shrink resources, disable `profileable`, and refuse cleartext traffic. Do not upload `:app:assembleRelease` — that artifact is unsigned and only for local inspection.

## 3. Build and sign the release bundle

```powershell
.\gradlew.bat clean :app:testDebugUnitTest :app:lintRelease :app:signedBundleRelease --no-daemon
```

Output: `app/build/outputs/bundle/release/app-release.aab`  
Mapping file: `app/build/outputs/mapping/release/mapping.txt`

`signedBundleRelease` fails if the upload key is missing or the bundle is unsigned.

## 4. Test the release candidate

Install that signed bundle (or a Play internal-track build) on:

- at least one phone
- at least one tablet

Check camera permission, capture, photo picker import, save, export/share, and Settings → Privacy / Terms. Firebase Test Lab is optional extra coverage.

## 5. Release on Google Play

Play’s three steps:

1. **Promotional materials** — listing copy, icon, feature graphic, screenshots. Validate sizes with `.\scripts\validate-play-listing.ps1`. Retake screenshots from the signed candidate; current PNGs are dimension-valid drafts of an older UI.
2. **Configure and upload** — create the app, category **Art & Design**, ads **No**, privacy URL `https://github.com/vishnu-17o7/campalette/blob/main/PRIVACY.md`, then fill [play/console.md](play/console.md). Upload the `.aab` to a testing track. Enroll **Play App Signing**.
3. **Publish** — only after review settings look right.

### Account and policy gates you must complete in Console

1. Pay the Play developer fee and finish identity verification. Creating a Play app also registers the package for [Android developer verification](https://developer.android.com/developer-verification) (required for installs on certified devices in Brazil, Indonesia, Singapore, and Thailand from 30 September 2026, then globally).
2. Merge this branch to `main` so privacy and terms URLs return 200.
3. Optional: GitHub Pages from `/docs` for `https://vishnu-17o7.github.io/campalette/privacy.html`.
4. Personal Play accounts created after 13 November 2023 need a **closed test with 12 opted-in testers for 14 consecutive days** before production unlocks.
5. Staged production rollout: 20% → watch crashes, ANRs, camera bind failures → 50% → 100%.

Do not distribute the app by hosting an APK on a website unless you also handle unknown-sources installs. Play is the intended store.

## After each later release

Bump `versionCode` and `versionName`, add `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`, and upload a new AAB signed with the same upload key.
