package com.tanzirdev.modmase.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanzirdev.modmase.AppState
import com.tanzirdev.modmase.Links
import com.tanzirdev.modmase.Prefs
import com.tanzirdev.modmase.Push
import com.tanzirdev.modmase.openTelegram
import com.tanzirdev.modmase.openUrl
import com.tanzirdev.modmase.shareText
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(icon: ImageBitmap) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val version = remember {
        try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "2.0.0"
        } catch (e: Exception) {
            "2.0.0"
        }
    }
    val highlights = remember {
        listOf(
            "🔧" to "Android MODs & Tweaks",
            "📱" to "Useful Apps & Tools",
            "🎮" to "Games & Game Mods",
            "⚡" to "Latest Updates & Releases",
            "🛠️" to "Modding Resources & Guides"
        )
    }
    val topicNames = remember {
        listOf(
            "news" to "📰 News & announcements",
            "mods" to "🔧 New MODs",
            "apps" to "📱 New apps",
            "games" to "🎮 New games",
            "tools" to "🧰 Tools"
        )
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        Prefs.setPush(granted)
        Push.syncTopics()
        if (!granted) Toaster.show("Notifications are blocked in system settings", ToastType.Info)
    }

    ScreenColumn {
        Reveal(0) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                RoundIcon(icon, size = 96)
                Spacer(Modifier.height(14.dp))
                MText("MODMASE", size = 26.sp, weight = FontWeight.Bold, letterSpacing = 3.sp)
                MText("Version $version", size = 12.sp, color = MColors.Muted)
            }
        }

        Reveal(1) { SectionTitle("Settings") }
        Reveal(2) {
            GlassCard {
                SettingSwitch("Haptic feedback", "Light vibration when you tap", Prefs.haptics) { Prefs.setHaptics(it) }
                if (Build.VERSION.SDK_INT >= 31) {
                    SettingSwitch(
                        "Material You colors",
                        "Use your wallpaper colors instead of MODMASE green",
                        Prefs.materialYou
                    ) { Prefs.setMaterialYou(it) }
                }
                SettingSwitch("Push notifications", "Get notified about new releases", Prefs.pushEnabled) { on ->
                    if (on && Build.VERSION.SDK_INT >= 33 && !Push.notificationsAllowed(ctx)) {
                        permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        Prefs.setPush(on)
                        Push.syncTopics()
                    }
                }
                if (Prefs.pushEnabled) {
                    Spacer(Modifier.height(6.dp))
                    MText("Notify me about", size = 11.sp, color = MColors.Muted)
                    topicNames.forEach { (key, label) ->
                        SettingSwitch(label, "", Prefs.topics[key] != false) { on ->
                            Prefs.setTopic(key, on)
                            Push.syncTopics()
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Reveal(3) {
            GlowButton("Check for updates", "🔄", false, {
                scope.launch {
                    val has = AppState.manualCheck(ctx)
                    if (has) {
                        Toaster.show("A new version is available!", ToastType.Success)
                    } else {
                        Toaster.show("You're on the latest version", ToastType.Success)
                    }
                }
            }, Modifier.fillMaxWidth())
        }

        Reveal(4) { SectionTitle("Welcome to MODMASE") }
        Reveal(5) {
            GlassCard {
                MText("🚀 Your Modding Destination!", size = 15.sp, weight = FontWeight.SemiBold, color = MColors.Lime)
                Spacer(Modifier.height(8.dp))
                MText("Welcome to MODMASE - a place for MODs, Apps, Games & Tools.", size = 13.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(16.dp))
                MText("✨ WHAT YOU'LL FIND HERE", size = 12.sp, weight = FontWeight.SemiBold, color = MColors.Muted, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                highlights.forEach { (emoji, text) ->
                    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        MText(emoji, size = 16.sp)
                        Spacer(Modifier.width(12.dp))
                        MText(text, size = 13.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                MText("💚 Stay tuned. Discover. Mod. Enjoy.", size = 13.sp, color = MColors.Lime, weight = FontWeight.Medium)
            }
        }

        Reveal(6) { SectionTitle("About the modder") }
        Reveal(7) {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundIcon(icon, size = 56)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        MText("MODMASE Team", size = 16.sp, weight = FontWeight.Bold)
                        MText("Modders · Curators · Android fans", size = 11.sp, color = MColors.Muted)
                    }
                }
                Spacer(Modifier.height(14.dp))
                MText(
                    "We hunt down the best Android mods, apps, games and tools, then share them with the community along with guides to help you mod better and live smarter.",
                    size = 12.sp, color = MColors.Muted, lineHeight = 18.sp
                )
            }
        }

        Reveal(8) { SectionTitle("Find us") }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GlowButton("Join Telegram  ·  t.me/MODMASE", "📢", true, { openTelegram(ctx) }, Modifier.fillMaxWidth())
            GlowButton("modmase.vercel.app", "🌐", false, { openUrl(ctx, Links.WEBSITE) }, Modifier.fillMaxWidth())
            GlowButton(
                "Share MODMASE", "💚", false,
                { shareText(ctx, "MODMASE - Your Modding Destination!\n\nTelegram: ${Links.TELEGRAM}\nWebsite: ${Links.WEBSITE}") },
                Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(26.dp))
        MText(
            "Made with 💚 by MODMASE",
            modifier = Modifier.fillMaxWidth(),
            size = 12.sp,
            color = MColors.Muted,
            align = TextAlign.Center
        )
    }
}
