# MODMASE App

Android app for the MODMASE Telegram channel - Kotlin + Jetpack Compose.

- Telegram: https://t.me/MODMASE
- Website: https://modmase.vercel.app

## Build the APK with GitHub Actions
1. Create a new GitHub repository and upload everything in this folder (keep the `.github` folder).
2. Push to `main` (or open the **Actions** tab -> **Build MODMASE APK** -> **Run workflow**).
3. When the run turns green, open it and download `MODMASE-release-apk` (or `MODMASE-debug-apk`) from **Artifacts**.

## Where things live
- `app/src/main/assets/fonts` - Poppins + Pacifico fonts
- `app/src/main/assets/images` - `modmase_banner.jpg`, `modmase_icon.png`
- `app/src/main/res/mipmap-*` - launcher icons (square + round)
- `app/src/main/java/com/tanzirdev/modmase` - source code (links are in `Utils.kt` -> `Links`)
