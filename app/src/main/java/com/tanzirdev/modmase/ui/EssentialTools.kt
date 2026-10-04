package com.tanzirdev.modmase.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.tanzirdev.modmase.Api
import com.tanzirdev.modmase.Links
import com.tanzirdev.modmase.Prefs
import com.tanzirdev.modmase.copyText
import com.tanzirdev.modmase.shareFileBytes
import com.tanzirdev.modmase.shareText
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/* =====================================================================
 *  1. HASH CHECKER
 * ===================================================================== */

fun hashStream(ctx: Context, uri: Uri, size: Long, onProgress: (Float) -> Unit): List<String>? {
    return try {
        val md5 = MessageDigest.getInstance("MD5")
        val sha1 = MessageDigest.getInstance("SHA-1")
        val sha256 = MessageDigest.getInstance("SHA-256")
        val stream = ctx.contentResolver.openInputStream(uri) ?: return null
        stream.use { s ->
            val buf = ByteArray(1 shl 16)
            var total = 0L
            var n = s.read(buf)
            while (n != -1) {
                md5.update(buf, 0, n)
                sha1.update(buf, 0, n)
                sha256.update(buf, 0, n)
                total += n
                if (size > 0) onProgress((total.toFloat() / size.toFloat()).coerceIn(0f, 1f))
                n = s.read(buf)
            }
        }
        listOf(md5.digest().toHex(), sha1.digest().toHex(), sha256.digest().toHex())
    } catch (e: Exception) {
        null
    }
}

@Composable
fun HashCheckerTool(expectedInit: String, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var fileName by remember { mutableStateOf("") }
    var fileSize by remember { mutableLongStateOf(0L) }
    var progress by remember { mutableFloatStateOf(0f) }
    var busy by remember { mutableStateOf(false) }
    var md5 by remember { mutableStateOf("") }
    var sha1 by remember { mutableStateOf("") }
    var sha256 by remember { mutableStateOf("") }
    var expected by remember { mutableStateOf(expectedInit) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                busy = true
                progress = 0f
                md5 = ""
                sha1 = ""
                sha256 = ""
                val info = withContext(Dispatchers.IO) { fileInfo(ctx, uri) }
                fileName = info.first
                fileSize = info.second
                val res = withContext(Dispatchers.IO) { hashStream(ctx, uri, info.second) { p -> progress = p } }
                if (res == null) {
                    Toaster.show("Could not read this file", ToastType.Error)
                } else {
                    md5 = res[0]
                    sha1 = res[1]
                    sha256 = res[2]
                    progress = 1f
                    Toaster.show("Hashes ready", ToastType.Success)
                }
                busy = false
            }
        }
    }

    val exp = expected.trim().lowercase().replace(" ", "").replace(":", "")
    val matchedWith: String? = when {
        sha256.isEmpty() || exp.isEmpty() -> null
        exp == sha256 -> "SHA-256"
        exp == sha1 -> "SHA-1"
        exp == md5 -> "MD5"
        else -> ""
    }

    ToolScaffold("Hash Checker", "🔐", onBack) {
        InfoNote("Pick any file (APK, zip...) to calculate MD5, SHA-1 and SHA-256 on your device. No permission needed and nothing is uploaded.")
        GapV()
        GlowButton(if (busy) "Calculating..." else "Select file", "📂", true, {
            if (!busy) picker.launch(arrayOf("*/*"))
        }, Modifier.fillMaxWidth())
        if (busy || fileName.isNotEmpty()) {
            GapV()
            GlassCard {
                MText(fileName, size = 13.sp, weight = FontWeight.SemiBold, maxLines = 2)
                MText(formatBytes(fileSize), size = 11.sp, color = MColors.Muted)
                GapV(10)
                ProgressBar(progress)
            }
        }
        if (sha256.isNotEmpty()) {
            GapV()
            GlassCard {
                ResultRow("SHA-256", sha256)
                ResultRow("SHA-1", sha1)
                ResultRow("MD5", md5)
            }
        }
        GapV()
        ToolInput(expected, { expected = it }, "Expected hash (optional - paste to compare)", mono = true, minLines = 2)
        if (matchedWith != null) {
            GapV(10)
            val ok = matchedWith.isNotEmpty()
            GlassCard {
                MText(
                    if (ok) "✅ Match ($matchedWith). The file is identical." else "❌ Does not match. Do not install this file.",
                    size = 14.sp,
                    weight = FontWeight.Bold,
                    color = if (ok) MColors.Lime else MColors.Danger
                )
            }
        }
    }
}

/* =====================================================================
 *  2. DEVICE INFO EXPORT
 * ===================================================================== */

class DeviceStats(val ramUsed: Long, val ramTotal: Long, val storageUsed: Long, val storageTotal: Long)

fun readStats(ctx: Context): DeviceStats {
    val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    val mi = android.app.ActivityManager.MemoryInfo()
    am.getMemoryInfo(mi)
    val stat = StatFs(Environment.getDataDirectory().path)
    val total = stat.blockCountLong * stat.blockSizeLong
    val free = stat.availableBlocksLong * stat.blockSizeLong
    return DeviceStats(mi.totalMem - mi.availMem, mi.totalMem, total - free, total)
}

fun gbText(bytes: Long): String = String.format(java.util.Locale.US, "%.1f GB", bytes / 1073741824.0)

fun buildDeviceReport(ctx: Context): String {
    val m = ctx.resources.displayMetrics
    val st = readStats(ctx)
    return buildString {
        appendLine("MODMASE - Device report")
        appendLine("Generated: ${Date()}")
        appendLine()
        appendLine("Device: ${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}")
        appendLine("Codename: ${Build.DEVICE}")
        appendLine("Brand: ${Build.BRAND}")
        appendLine("Board / hardware: ${Build.HARDWARE}")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("Security patch: ${Build.VERSION.SECURITY_PATCH ?: "unknown"}")
        appendLine("Build ID: ${Build.ID}")
        appendLine("Supported ABIs: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
        appendLine("Display: ${m.widthPixels} x ${m.heightPixels} @ ${m.densityDpi} dpi (density ${m.density})")
        appendLine("RAM: ${gbText(st.ramUsed)} used of ${gbText(st.ramTotal)}")
        appendLine("Storage: ${gbText(st.storageUsed)} used of ${gbText(st.storageTotal)}")
    }
}

@Composable
fun DeviceExportTool(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val report = remember { buildDeviceReport(ctx) }
    val saver = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        if (uri != null) {
            try {
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(report.toByteArray()) }
                Toaster.show("Saved as text file", ToastType.Success)
            } catch (e: Exception) {
                Toaster.show("Couldn't save the file", ToastType.Error)
            }
        }
    }
    ToolScaffold("Device Export", "📤", onBack) {
        GlassCard {
            MText(report, size = 11.sp, lineHeight = 17.sp, color = MColors.TextPrimary)
        }
        GapV()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlowButton("Copy", "📋", false, { copyText(ctx, "Device info", report) }, Modifier.weight(1f))
            GlowButton("Share text", "📤", false, { shareText(ctx, report) }, Modifier.weight(1f))
        }
        GapV(10)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlowButton("Share .txt", "📎", true, {
                shareFileBytes(ctx, "modmase-device-info.txt", report.toByteArray(), "text/plain")
            }, Modifier.weight(1f))
            GlowButton("Save .txt", "💾", true, { saver.launch("modmase-device-info.txt") }, Modifier.weight(1f))
        }
    }
}

/* =====================================================================
 *  3. BATTERY & NETWORK
 * ===================================================================== */

private fun batteryRows(ctx: Context): Pair<Float, List<Pair<String, String>>> {
    val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val pct = if (level >= 0 && scale > 0) level * 100f / scale else 0f
    val status = when (i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
        else -> "Unknown"
    }
    val plug = when (i?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC charger"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> "Not plugged"
    }
    val health = when (i?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }
    val temp = (i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
    val volt = (i?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
    val tech = i?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "-"
    return Pair(
        pct,
        listOf(
            "Status" to status,
            "Power source" to plug,
            "Health" to health,
            "Temperature" to "$temp °C",
            "Voltage" to "$volt V",
            "Technology" to tech
        )
    )
}

private fun networkRows(ctx: Context): List<Pair<String, String>> {
    return try {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork
        val caps = if (net != null) cm.getNetworkCapabilities(net) else null
        if (caps == null) {
            listOf("Connection" to "Offline")
        } else {
            val type = when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile data"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                else -> "Other"
            }
            listOf(
                "Connection" to type,
                "Internet access" to if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) "Verified" else "Unverified",
                "Metered" to if (cm.isActiveNetworkMetered) "Yes" else "No",
                "Est. download" to "${caps.linkDownstreamBandwidthKbps / 1000} Mbps",
                "Est. upload" to "${caps.linkUpstreamBandwidthKbps / 1000} Mbps"
            )
        }
    } catch (e: Exception) {
        listOf("Connection" to "Unavailable")
    }
}

@Composable
fun BatteryNetworkTool(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            tick++
        }
    }
    val battery = remember(tick) { batteryRows(ctx) }
    val network = remember(tick) { networkRows(ctx) }
    ToolScaffold("Battery & Network", "🔋", onBack) {
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MText("${battery.first.toInt()}%", size = 34.sp, weight = FontWeight.Bold, color = MColors.Lime)
                MText("  Battery", size = 13.sp, color = MColors.Muted)
            }
            GapV(10)
            ProgressBar(battery.first / 100f)
            GapV(10)
            battery.second.forEach { (k, v) -> ResultRow(k, v) }
        }
        GapV()
        MText("Network", size = 16.sp, weight = FontWeight.SemiBold)
        GapV(10)
        GlassCard {
            network.forEach { (k, v) -> ResultRow(k, v) }
        }
        GapV(10)
        InfoNote("Updates every 2 seconds. Wi-Fi name is hidden because Android requires a location permission for it.")
    }
}

/* =====================================================================
 *  4. QR GENERATOR
 * ===================================================================== */

fun qrBitmap(text: String, size: Int = 720): Bitmap? {
    return try {
        val hints = mapOf<EncodeHintType, Any>(
            EncodeHintType.MARGIN to 2,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
        )
        val m = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints)
        val w = m.width
        val h = m.height
        val px = IntArray(w * h)
        val dark = 0xFF050806.toInt()
        val light = 0xFFFFFFFF.toInt()
        for (y in 0 until h) {
            for (x in 0 until w) {
                px[y * w + x] = if (m.get(x, y)) dark else light
            }
        }
        Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun QrTool(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var text by remember { mutableStateOf(Links.TELEGRAM) }
    val bmp = remember(text) { if (text.isBlank()) null else qrBitmap(text) }
    ToolScaffold("QR Generator", "🔳", onBack) {
        InfoNote("Let a friend scan this code to join the MODMASE channel instantly.")
        GapV()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Telegram", text == Links.TELEGRAM) { text = Links.TELEGRAM }
            Chip("Website", text == Links.WEBSITE) { text = Links.WEBSITE }
        }
        GapV()
        ToolInput(text, { text = it }, "Text or link", mono = true, minLines = 2)
        GapV()
        if (bmp != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "QR code",
                    filterQuality = FilterQuality.None,
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(3.dp, MColors.Lime, RoundedCornerShape(20.dp))
                )
            }
            GapV()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlowButton("Share image", "🖼️", true, {
                    val out = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    shareFileBytes(ctx, "modmase-qr.png", out.toByteArray(), "image/png")
                }, Modifier.weight(1f))
                GlowButton("Share link", "🔗", false, { shareText(ctx, text) }, Modifier.weight(1f))
            }
        } else {
            MText("Enter some text to generate a QR code.", size = 12.sp, color = MColors.Muted)
        }
    }
}

/* =====================================================================
 *  5. MOD REQUEST
 * ===================================================================== */

@Composable
fun ModRequestTool(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var version by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }

    fun message(): String =
        "Mod request\nApp: ${name.trim()}\nVersion: ${version.trim().ifEmpty { "any" }}\n" +
            "Details: ${details.trim().ifEmpty { "-" }}\nContact: ${contact.trim().ifEmpty { "-" }}"

    ToolScaffold("Mod Request", "📝", onBack) {
        InfoNote("Tell us which mod or app you want. Your request goes straight to the MODMASE admin panel. You can also send it on Telegram.")
        GapV()
        ToolInput(name, { name = it }, "App / mod name *")
        GapV(10)
        ToolInput(version, { version = it }, "Version (optional)")
        GapV(10)
        ToolInput(details, { details = it }, "Which features do you want?", minLines = 3)
        GapV(10)
        ToolInput(contact, { contact = it }, "Your Telegram username (optional)")
        GapV(18)
        GlowButton(if (sending) "Sending..." else "Send request", "🚀", true, {
            if (sending) return@GlowButton
            if (name.trim().length < 2) {
                Toaster.show("Please enter the app or mod name", ToastType.Error)
                return@GlowButton
            }
            val now = System.currentTimeMillis()
            if (now - Prefs.getLong("last_request") < 60_000L) {
                Toaster.show("Please wait a minute before sending another request", ToastType.Info)
                return@GlowButton
            }
            sending = true
            scope.launch {
                val body = JSONObject()
                    .put("name", name.trim().take(120))
                    .put("version", version.trim().take(60))
                    .put("details", details.trim().take(800))
                    .put("contact", contact.trim().take(60))
                    .put("app", "2.0.0")
                    .put("createdAt", JSONObject().put(".sv", "timestamp"))
                val res = Api.send("POST", "requests", body.toString())
                sending = false
                if (res != null) {
                    Prefs.putLong("last_request", System.currentTimeMillis())
                    Toaster.show("Request sent. Thank you!", ToastType.Success)
                    name = ""
                    version = ""
                    details = ""
                } else {
                    Toaster.show("Couldn't send. Check your connection.", ToastType.Error)
                }
            }
        }, Modifier.fillMaxWidth())
        GapV(10)
        GlowButton("Send via Telegram instead", "✈️", false, {
            if (name.trim().length < 2) {
                Toaster.show("Please enter the app or mod name", ToastType.Error)
            } else {
                shareText(ctx, message() + "\n\n" + Links.TELEGRAM)
            }
        }, Modifier.fillMaxWidth())
    }
}
