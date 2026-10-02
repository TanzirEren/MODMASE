package com.tanzirdev.modmase.ui

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanzirdev.modmase.Links
import com.tanzirdev.modmase.copyText
import com.tanzirdev.modmase.openSystemSettings
import com.tanzirdev.modmase.openTelegram
import com.tanzirdev.modmase.openUrl
import com.tanzirdev.modmase.shareText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/* =====================================================================
 *  SPLASH
 * ===================================================================== */

@Composable
fun SplashScreen(icon: ImageBitmap) {
    val sc = remember { Animatable(0.55f) }
    val al = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { sc.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        al.animateTo(1f, tween(700))
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = sc.value
                scaleY = sc.value
                alpha = al.value
            }
        ) {
            RoundIcon(icon, size = 124)
            Spacer(Modifier.height(22.dp))
            MText("MODMASE", size = 32.sp, weight = FontWeight.Bold, letterSpacing = 5.sp)
            Spacer(Modifier.height(4.dp))
            MText("Mod Better, Live Smarter", script = true, size = 18.sp, color = MColors.Lime)
        }
    }
}

/* =====================================================================
 *  HOME
 * ===================================================================== */

private class Category(val emoji: String, val title: String, val subtitle: String)

@Composable
fun HomeScreen(banner: ImageBitmap, icon: ImageBitmap) {
    val ctx = LocalContext.current
    val categories = remember {
        listOf(
            Category("🔧", "Android MODs", "Tweaks & patches"),
            Category("📱", "Apps & Tools", "Useful picks"),
            Category("🎮", "Games", "Game mods"),
            Category("⚡", "Updates", "Fresh releases")
        )
    }

    ScreenColumn {
        Reveal(0) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RoundIcon(icon)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    MText("MODMASE", size = 22.sp, weight = FontWeight.Bold, letterSpacing = 1.sp)
                    MText("Your Modding Destination", size = 12.sp, color = MColors.Muted)
                }
                LiveBadge()
            }
        }
        Spacer(Modifier.height(22.dp))
        Reveal(1) { BannerCard(banner) }
        Spacer(Modifier.height(20.dp))
        Reveal(2) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                MText("Mod Better, Live Smarter", script = true, size = 24.sp, color = MColors.Lime)
                Spacer(Modifier.height(4.dp))
                MText(
                    "Mods · Apps · Tools · More",
                    size = 12.sp,
                    color = MColors.Muted,
                    letterSpacing = 2.sp
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Reveal(3) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlowButton("Join Telegram", "✈️", true, { openTelegram(ctx) }, Modifier.weight(1f))
                GlowButton("Open Website", "🌐", false, { openUrl(ctx, Links.WEBSITE) }, Modifier.weight(1f))
            }
        }

        Reveal(4) { SectionTitle("What you'll find here") }
        Reveal(5) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { cat ->
                            GlassCard(Modifier.weight(1f), onClick = { openTelegram(ctx) }) {
                                MText(cat.emoji, size = 26.sp)
                                Spacer(Modifier.height(10.dp))
                                MText(cat.title, size = 14.sp, weight = FontWeight.SemiBold)
                                MText(cat.subtitle, size = 11.sp, color = MColors.Muted)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Reveal(6) {
            GlassCard(onClick = { openTelegram(ctx) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MText("🛠️", size = 26.sp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        MText("Modding Resources & Guides", size = 14.sp, weight = FontWeight.SemiBold)
                        MText("Learn, tweak and build your own mods", size = 11.sp, color = MColors.Muted)
                    }
                }
            }
        }
        Spacer(Modifier.height(26.dp))
        Reveal(7) {
            MText(
                "💚 Stay tuned. Discover. Mod. Enjoy.",
                modifier = Modifier.fillMaxWidth(),
                size = 13.sp,
                color = MColors.Muted,
                align = TextAlign.Center
            )
        }
    }
}

/* =====================================================================
 *  TOOLS
 * ===================================================================== */

private val ModTips = listOf(
    "Always keep a backup of the original APK before trying a mod.",
    "Download mods from sources you trust and check what permissions they ask for.",
    "Clear an app's cache before updating a modded version to avoid conflicts.",
    "Keep Play Protect on - it can warn you about risky files.",
    "Read the post notes on the channel before installing anything new."
)

private class Shortcut(val emoji: String, val title: String, val action: String)

private class DeviceStats(
    val ramUsed: Long,
    val ramTotal: Long,
    val storageUsed: Long,
    val storageTotal: Long
)

private fun readStats(ctx: Context): DeviceStats {
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo()
    am.getMemoryInfo(mi)
    val stat = StatFs(Environment.getDataDirectory().path)
    val total = stat.blockCountLong * stat.blockSizeLong
    val free = stat.availableBlocksLong * stat.blockSizeLong
    return DeviceStats(mi.totalMem - mi.availMem, mi.totalMem, total - free, total)
}

private fun gb(bytes: Long): String =
    String.format(Locale.US, "%.1f GB", bytes / 1073741824.0)

@Composable
fun ToolsScreen() {
    val ctx = LocalContext.current
    val stats = remember { readStats(ctx) }
    val metrics = ctx.resources.displayMetrics

    val rows = remember {
        listOf(
            "Device" to "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            "Android" to "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "Board" to Build.HARDWARE,
            "CPU ABI" to (Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"),
            "Display" to "${metrics.widthPixels} x ${metrics.heightPixels} @ ${metrics.densityDpi} dpi",
            "Security patch" to (Build.VERSION.SECURITY_PATCH ?: "unknown")
        )
    }
    val shortcuts = remember {
        listOf(
            Shortcut("🧑‍💻", "Developer options", Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
            Shortcut("📦", "All apps", Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS),
            Shortcut("💾", "Storage", Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
            Shortcut("📱", "About phone", Settings.ACTION_DEVICE_INFO_SETTINGS)
        )
    }

    var tipIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4500)
            tipIndex = (tipIndex + 1) % ModTips.size
        }
    }

    ScreenColumn {
        Reveal(0) {
            Column {
                MText("Toolbox", size = 26.sp, weight = FontWeight.Bold)
                MText("Handy utilities for every modder", size = 12.sp, color = MColors.Muted)
            }
        }

        Reveal(1) { SectionTitle("Mod tip") }
        Reveal(2) {
            GlassCard {
                AnimatedContent(
                    targetState = tipIndex,
                    transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                    label = "tip"
                ) { i ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MText("💡", size = 22.sp)
                        Spacer(Modifier.width(12.dp))
                        MText(ModTips[i], size = 13.sp, lineHeight = 19.sp)
                    }
                }
            }
        }

        Reveal(3) { SectionTitle("Device monitor") }
        Reveal(4) {
            GlassCard {
                Meter("Memory (RAM)", stats.ramUsed, stats.ramTotal)
                Spacer(Modifier.height(16.dp))
                Meter("Internal storage", stats.storageUsed, stats.storageTotal)
                Spacer(Modifier.height(18.dp))
                rows.forEach { (label, value) -> InfoRow(label, value) }
                Spacer(Modifier.height(14.dp))
                GlowButton(
                    text = "Copy device info",
                    emoji = "📋",
                    filled = false,
                    onClick = {
                        copyText(ctx, "Device info", rows.joinToString("\n") { "${it.first}: ${it.second}" })
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Reveal(5) { SectionTitle("Quick shortcuts") }
        Reveal(6) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                shortcuts.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { s ->
                            GlassCard(Modifier.weight(1f), onClick = { openSystemSettings(ctx, s.action) }) {
                                MText(s.emoji, size = 24.sp)
                                Spacer(Modifier.height(8.dp))
                                MText(s.title, size = 13.sp, weight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        Reveal(7) { SectionTitle("Share & copy") }
        Reveal(8) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlowButton("Copy Telegram", "✈️", false, { copyText(ctx, "Telegram link", Links.TELEGRAM) }, Modifier.weight(1f))
                    GlowButton("Copy Website", "🌐", false, { copyText(ctx, "Website link", Links.WEBSITE) }, Modifier.weight(1f))
                }
                GlowButton(
                    text = "Share MODMASE with friends",
                    emoji = "💚",
                    filled = true,
                    onClick = {
                        shareText(
                            ctx,
                            "MODMASE - Your Modding Destination!\n\nTelegram: ${Links.TELEGRAM}\nWebsite: ${Links.WEBSITE}"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun Meter(label: String, used: Long, total: Long) {
    val target = if (total > 0) (used.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    var start by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        start = true
    }
    val progress by animateFloatAsState(
        targetValue = if (start) target else 0f,
        animationSpec = tween(1200),
        label = "meter"
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        MText(label, size = 13.sp, weight = FontWeight.Medium)
        MText("${gb(used)} / ${gb(total)}", size = 12.sp, color = MColors.Muted)
    }
    Spacer(Modifier.height(8.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(MColors.Bg)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(MColors.DeepGreen, MColors.Green, MColors.Lime)))
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MText(label, size = 12.sp, color = MColors.Muted)
        Spacer(Modifier.width(12.dp))
        MText(value, size = 12.sp, weight = FontWeight.Medium, align = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

/* =====================================================================
 *  ABOUT
 * ===================================================================== */

@Composable
fun AboutScreen(icon: ImageBitmap) {
    val ctx = LocalContext.current
    val version = remember {
        try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
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

    ScreenColumn {
        Reveal(0) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                RoundIcon(icon, size = 96)
                Spacer(Modifier.height(14.dp))
                MText("MODMASE", size = 26.sp, weight = FontWeight.Bold, letterSpacing = 3.sp)
                MText("Version $version", size = 12.sp, color = MColors.Muted)
            }
        }

        Reveal(1) { SectionTitle("Welcome to MODMASE") }
        Reveal(2) {
            GlassCard {
                MText("🚀 Your Modding Destination!", size = 15.sp, weight = FontWeight.SemiBold, color = MColors.Lime)
                Spacer(Modifier.height(8.dp))
                MText(
                    "Welcome to MODMASE - a place for MODs, Apps, Games & Tools.",
                    size = 13.sp,
                    lineHeight = 20.sp
                )
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

        Reveal(3) { SectionTitle("About the modder") }
        Reveal(4) {
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
                    size = 12.sp,
                    color = MColors.Muted,
                    lineHeight = 18.sp
                )
            }
        }

        Reveal(5) { SectionTitle("Find us") }
        Reveal(6) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlowButton("Join Telegram  ·  t.me/MODMASE", "📢", true, { openTelegram(ctx) }, Modifier.fillMaxWidth())
                GlowButton("modmase.vercel.app", "🌐", false, { openUrl(ctx, Links.WEBSITE) }, Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.height(26.dp))
        Reveal(7) {
            MText(
                "Made with 💚 by MODMASE",
                modifier = Modifier.fillMaxWidth(),
                size = 12.sp,
                color = MColors.Muted,
                align = TextAlign.Center
            )
        }
    }
}
