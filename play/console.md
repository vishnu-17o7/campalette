# Play Console answers for Campalette 1.0.0

Use these answers in Google Play Console. They match the current app: no accounts, no ads, no analytics, camera + photo picker, local palettes, optional Android backup.

## App details

| Field | Value |
|---|---|
| Application ID | `com.vishnu.campalette` |
| Default language | English (United States) |
| App name | Campalette |
| Short description | Capture real-world colors and turn them into polished, reusable palettes. |
| App category | Art &amp; Design |
| Tags | color palette, camera, design tools, hex, photography |
| Contact email | The Play developer account email (required) |
| Website | `https://github.com/vishnu-17o7/campalette` |
| Privacy policy | `https://github.com/vishnu-17o7/campalette/blob/main/PRIVACY.md` after this file is on `main`. Optional nicer URL: `https://vishnu-17o7.github.io/campalette/privacy.html` after GitHub Pages is enabled on `/docs`. |
| Terms of use | `https://github.com/vishnu-17o7/campalette/blob/main/EULA.md` (same merge requirement). |

Store listing copy and images live in `fastlane/metadata/android/en-US`.

## Ads

Does this app contain ads? **No.**

## App access

All functionality is available without an account. No login, no restricted parts.

## Content ratings (IARC questionnaire)

Campalette is a utility for capturing and organizing colors.

- Violence, sexual content, language, controlled substances, gambling: **None**
- User-to-user communication: **No**
- Shares user location: **No**
- Digital purchases / loot boxes: **No**
- Age category expected: **Everyone** (utility, not a children’s app)

In Target audience, do **not** select “Primarily for children.” Campalette is for designers and photographers, not a kids app.

## News / government / health / finance / crypto / VPN

All **No**.

## Photos and videos permissions

Campalette does **not** request `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, or `READ_EXTERNAL_STORAGE`. Gallery import uses the system photo picker. If Console still shows this form, choose the photo-picker option and describe: “Users pick one image through Android’s photo picker so Campalette can extract a palette on the device.”

## Camera permission declaration

If Console asks why the app uses the camera:

> Campalette’s core feature is capturing a scene and extracting a color palette. The camera preview and captured frames are processed on the device. Images are not uploaded to Campalette servers. The camera hardware feature is optional so the app can still be installed on devices without a camera; those users can import a photo instead.

## Data safety

Does the app collect or share user data required by this form?

**Yes, limited to Android backup of local app state.** Camera frames and picked photos are processed on-device and are not sent to the developer. The share sheet is a user-initiated transfer and is not declared as developer sharing.

### Collected (via Android Auto Backup / device transfer, not sent to the developer)

| Data type | Collected | Shared by developer | Purpose | Optional | Encrypted in transit | Users can request deletion |
|---|---|---|---|---|---|---|
| User-generated content (saved palettes, names, hex values, capture history) | Yes, stored on device and included in Android backup if the user has backup enabled | No | App functionality | No (needed to restore palettes) | Yes (Android backup) | Yes — uninstall, or delete the Google backup |
| App settings (theme, haptics, palette size, reduced motion) | Yes, same as above | No | App functionality | No | Yes (Android backup) | Yes |

### Not collected

Photos and videos, location, personal info, financial info, health, messages, contacts, app activity, web browsing, app performance / crash logs, device or other IDs, advertising ID.

### Other answers

- Data is sold: **No**
- Data is used for ads or remarketing: **No**
- Data is used for fraud prevention / security / compliance: **No** (no developer-operated backend)
- Data is processed ephemerally: camera frames and picked photos are processed on device and are not uploaded. Do not mark palettes as ephemeral; they persist until the user deletes them.
- Users can request deletion: **Yes** — uninstalling the app deletes local data; Google account backup settings cover Android backup copies.
- Independent security review: **No**

## Data deletion (Console form)

Users delete palettes in the app. Uninstalling removes remaining local data. Point the deletion instructions at the privacy policy section “Data deletion.” There is no developer-operated account to delete.

## Government apps, Health apps, Financial features, Crypto

Submit **No** / not applicable.

## Store presence checks

- Phone screenshots: at least two, 24-bit PNG, 1080×1920 drafts are in `fastlane/metadata/android/en-US/images/phoneScreenshots`.
- 10-inch tablet screenshots: four drafts at 1440×2560.
- High-res icon: 512×512, 24-bit PNG, no alpha.
- Feature graphic: 1024×500, 24-bit PNG, no alpha.
- Recapture screenshots from the signed 1.0.0 candidate before production. Current drafts predate the latest camera and settings UI.
