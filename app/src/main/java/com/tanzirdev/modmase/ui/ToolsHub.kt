package com.tanzirdev.modmase.ui

import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanzirdev.modmase.openSystemSettings
import kotlinx.coroutines.delay

class ToolDef(val id: String, val emoji: String, val title: String, val desc: String)

val EssentialTools = listOf(
    ToolDef("hash", "🔐", "Hash Checker", "SHA-256 / MD5 of any file"),
    ToolDef("device", "📤", "Device Export", "Share or save device info"),
    ToolDef("battery", "🔋", "Battery & Network", "Live status"),
    ToolDef("qr", "🔳", "QR Generator", "Share the channel"),
    ToolDef("request", "📝", "Mod Request", "Ask for a mod")
)

val ModderTools = listOf(
    ToolDef("apkinfo", "📦", "APK Inspector", "Package, version, SDK, signer"),
    ToolDef("perms", "🛡️", "Permission Scanner", "Spot risky permissions"),
    ToolDef("sigcmp", "✍️", "Signature Compare", "Original vs re-signed"),
    ToolDef("vercmp", "🔢", "Version Comparator", "Which version is newer"),
    ToolDef("b64", "🔤", "Base64 Tool", "Encode and decode"),
    ToolDef("texthash", "#️⃣", "Text Hash", "MD5, SHA-1, SHA-256, SHA-512"),
    ToolDef("base", "🧮", "Hex / Bin / Dec", "Number and text converter"),
    ToolDef("color", "🎨", "Color Converter", "HEX, ARGB, HSV, Compose"),
    ToolDef("dppx", "📐", "dp / px Converter", "All Android densities"),
    ToolDef("json", "🧾", "JSON Formatter", "Format and minify"),
    ToolDef("api", "📊", "API Level Guide", "Android versions table"),
    ToolDef("adb", "💻", "ADB & Build Cheats", "Copy-ready commands")
)

@Composable
fun ToolPage(id: String, expected: String, onBack: () -> Unit) {
    when (id) {
        "hash" -> HashCheckerTool(expected, onBack)
        "device" -> DeviceExportTool(onBack)
        "battery" -> BatteryNetworkTool(onBack)
        "qr" -> QrTool(onBack)
        "request" -> ModRequestTool(onBack)
        "apkinfo" -> ApkInspectorTool(onBack)
        "perms" -> PermissionScannerTool(onBack)
        "sigcmp" -> SignatureCompareTool(onBack)
        "vercmp" -> VersionCompareTool(onBack)
        "b64" -> Base64Tool(onBack)
        "texthash" -> TextHashTool(onBack)
        "base" -> BaseConverterTool(onBack)
        "color" -> ColorConverterTool(onBack)
        "dppx" -> DpPxTool(onBack)
        "json" -> JsonFormatterTool(onBack)
        "api" -> ApiLevelsTool(onBack)
        "adb" -> AdbCheatTool(onBack)
        else -> ToolScaffold("Tool", "🧰", onBack) { MText("Unknown tool", size = 13.sp) }
    }
}

private val ModTips = listOf(
    "Always keep a backup of the original APK before trying a mod.",
    "Download mods from sources you trust and check what permissions they ask for.",
    "Verify the SHA-256 of a download with the Hash Checker before installing.",
    "Keep Play Protect on. It can warn you about risky files.",
    "Read the post notes on the channel before installing anything new."
)

private class Shortcut(val emoji: String, val title: String, val action: String)

@Composable
fun ToolsScreen(onOpenTool: (String) -> Unit) {
    val ctx = LocalContext.current
    val stats = remember { readStats(ctx) }
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
                MText("Tap an icon to open a tool", size = 12.sp, color = MColors.Muted)
            }
        }

        // Quick icon row (top)
        Spacer(Modifier.height(18.dp))
        Reveal(1) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EssentialTools.forEach { t -> ToolIcon(t) { onOpenTool(t.id) } }
            }
        }

        Reveal(2) { SectionTitle("Modder toolkit") }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ModderTools.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { t ->
                        GlassCard(Modifier.weight(1f), onClick = { onOpenTool(t.id) }) {
                            MText(t.emoji, size = 24.sp)
                            Spacer(Modifier.height(8.dp))
                            MText(t.title, size = 13.sp, weight = FontWeight.SemiBold, maxLines = 1)
                            MText(t.desc, size = 10.sp, color = MColors.Muted, lineHeight = 14.sp, maxLines = 2)
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        Reveal(3) { SectionTitle("Mod tip") }
        GlassCard {
            androidx.compose.animation.AnimatedContent(
                targetState = tipIndex,
                transitionSpec = {
                    androidx.compose.animation.fadeIn(tween(400)) togetherWith androidx.compose.animation.fadeOut(tween(400))
                },
                label = "tip"
            ) { i ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MText("💡", size = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    MText(ModTips[i], size = 13.sp, lineHeight = 19.sp)
                }
            }
        }

        Reveal(4) { SectionTitle("Device monitor") }
        GlassCard {
            Meter("Memory (RAM)", stats.ramUsed, stats.ramTotal)
            Spacer(Modifier.height(16.dp))
            Meter("Internal storage", stats.storageUsed, stats.storageTotal)
        }

        Reveal(5) { SectionTitle("Quick shortcuts") }
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
}

@Composable
private fun ToolIcon(tool: ToolDef, onClick: () -> Unit) {
    val haptic = rememberHaptic()
    Column(
        Modifier
            .width(86.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                haptic()
                onClick()
            }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.linearGradient(listOf(MColors.SurfaceHigh, MColors.Surface)))
                .border(1.2.dp, Brush.linearGradient(listOf(MColors.Lime, MColors.DeepGreen)), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            MText(tool.emoji, size = 28.sp)
        }
        Spacer(Modifier.height(6.dp))
        MText(
            tool.title,
            size = 10.sp,
            weight = FontWeight.Medium,
            align = TextAlign.Center,
            lineHeight = 13.sp,
            maxLines = 2
        )
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
        MText(gbText(used) + " / " + gbText(total), size = 12.sp, color = MColors.Muted)
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
