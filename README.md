<div align="center">

<img src="app/src/main/assets/images/modmase_icon.png" width="110" alt="MODMASE icon" style="border-radius:50%"/>

# MODMASE v2.0.0

**🚀 Your Modding Destination!**

Official Android app of the MODMASE Telegram channel: live updates, apps library, push notifications and modder tools.

![Platform](https://img.shields.io/badge/platform-Android-3FD11F?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-8CFF2E?style=for-the-badge&logo=kotlin&logoColor=black)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-1B8A2E?style=for-the-badge)
![Version](https://img.shields.io/badge/version-2.0.0-050806?style=for-the-badge)

[📢 Telegram](https://t.me/MODMASE) · [🌐 Website](https://modmase.vercel.app)

<img src="app/src/main/assets/images/modmase_banner.jpg" alt="MODMASE banner"/>

</div>

---

## ✨ What's new in v2.0.0

- 📰 **Live updates feed** managed from the admin panel (title, image, category, "Open in Telegram")
- 🔔 **Push notifications** (Firebase Cloud Messaging) with per-category topics
- 🔄 **In-app update checker** (admin controlled, with GitHub release fallback, optional force update)
- 📱 **Apps library**: MODs / Apps / Games / Tools, search, filters, featured, screenshots, SHA-256, downloads
- 🎬 Lottie splash, swipeable onboarding, shimmer loading, haptic feedback, animated toasts
- 🎨 Optional **Material You** colors
- 📌 App icon shortcuts (Join Telegram, Website, Apps, Tools) and a **home screen widget**
- 🧰 **Toolbox**: Hash Checker, Device Export, Battery & Network, QR Generator, Mod Request
- 🛠️ **12 modder tools**: APK Inspector, Permission Scanner, Signature Compare, Version Comparator, Base64, Text Hash, Hex/Bin/Dec, Color Converter, dp/px, JSON Formatter, API Level Guide, ADB cheats

## 🏗️ Build with GitHub Actions
1. Push this project to GitHub (keep `.github`).
2. **Actions -> Build MODMASE APK -> Run workflow**.
3. Download `MODMASE-release-apk` from **Artifacts**.

### Enable push notifications
1. Firebase Console -> Project settings -> **Add app -> Android** -> package `com.tanzirdev.modmase`.
2. Download **`google-services.json`** and place it at `app/google-services.json`, then commit.
3. Without this file the app builds and runs normally, push is just inactive.

The admin panel, database rules and push API live in the website project (see its `SETUP.md`).

## 🧱 Tech stack
Kotlin 1.9.24 · Jetpack Compose (BOM 2024.06.00) · Material 3 · Firebase Messaging · Lottie · ZXing core · AGP 8.5.2 · Gradle 8.7 · JDK 17 · `minSdk 24` / `targetSdk 34`

## 📁 Structure
```
app/src/main/
├── assets/ (fonts, images, lottie)
├── res/ (icons, shortcuts, widget, themes)
└── java/com/tanzirdev/modmase/
    ├── MainActivity.kt, AppState.kt, Api.kt, Models.kt, Prefs.kt, Push.kt
    ├── ModmaseMessagingService.kt, ModmaseWidget.kt, Utils.kt
    └── ui/ (Theme, Components, Home, Apps, ToolsHub, EssentialTools, ModderTools, About, Onboarding)
```

## 🔐 Signing note
The release APK is signed with the standard debug key so it installs directly. Use your own keystore for store publishing.

---
<div align="center"><b>Stay tuned. Discover. Mod. Enjoy.</b><br>— MODMASE Team</div>
