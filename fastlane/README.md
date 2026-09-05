# Fastlane metadata

English Play listing copy and images live under `metadata/android/en-US`.

Uploading the Android App Bundle is done with Gradle, not Fastlane:

```powershell
.\gradlew.bat :app:signedBundleRelease
```

To push listing text and images from this folder you need a Play Developer API service account JSON (gitignored). Then:

```bash
fastlane supply --skip_upload_apk true --skip_upload_aab true
```

Leave Fastlane unused if you prefer to paste listing fields in Play Console by hand. The files here are still the source of truth for copy and store art.
