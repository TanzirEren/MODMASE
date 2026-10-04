package com.tanzirdev.modmase.ui

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.PackageInfoCompat
import com.tanzirdev.modmase.compareVersions
import com.tanzirdev.modmase.copyText
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/* =====================================================================
 *  APK analysis (read-only: nothing is modified or installed)
 * ===================================================================== */

class ApkReport(
    val label: String,
    val pkg: String,
    val versionName: String,
    val versionCode: Long,
    val minSdk: Int,
    val targetSdk: Int,
    val sizeBytes: Long,
    val permissions: List<String>,
    val signers: List<String>,
    val sha256: String
)

@Suppress("DEPRECATION")
private fun signingFlag(): Int =
    if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES

@Suppress("DEPRECATION")
private fun signerHashes(info: PackageInfo): List<String> {
    val sigs: Array<Signature>? =
        if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
    return sigs?.map { digestHex("SHA-256", it.toByteArray()).chunked(2).joinToString(":").uppercase() }
        ?: emptyList()
}

object ApkAnalyzer {
    fun analyze(ctx: Context, uri: Uri): ApkReport? {
        return try {
            val out = File(ctx.cacheDir, "inspect_" + System.nanoTime() + ".apk")
            val md = MessageDigest.getInstance("SHA-256")
            val input = ctx.contentResolver.openInputStream(uri) ?: return null
            input.use { ins ->
                out.outputStream().use { o ->
                    val buf = ByteArray(1 shl 16)
                    var n = ins.read(buf)
                    while (n != -1) {
                        o.write(buf, 0, n)
                        md.update(buf, 0, n)
                        n = ins.read(buf)
                    }
                }
            }
            val pm = ctx.packageManager
            val info = pm.getPackageArchiveInfo(out.absolutePath, PackageManager.GET_PERMISSIONS or signingFlag())
            if (info == null) {
                out.delete()
                return null
            }
            val ai = info.applicationInfo
            ai?.sourceDir = out.absolutePath
            ai?.publicSourceDir = out.absolutePath
            val label = try {
                ai?.loadLabel(pm)?.toString() ?: ""
            } catch (e: Exception) {
                ""
            }
            val report = ApkReport(
                label = label.ifBlank { "(unknown)" },
                pkg = info.packageName ?: "",
                versionName = info.versionName ?: "",
                versionCode = PackageInfoCompat.getLongVersionCode(info),
                minSdk = ai?.minSdkVersion ?: 0,
                targetSdk = ai?.targetSdkVersion ?: 0,
                sizeBytes = out.length(),
                permissions = info.requestedPermissions?.toList() ?: emptyList(),
                signers = signerHashes(info),
                sha256 = md.digest().toHex()
            )
            out.delete()
            report
        } catch (e: Throwable) {
            null
        }
    }
}

@Composable
fun ApkPicker(buttonText: String, onResult: (ApkReport?) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                busy = true
                val r = withContext(Dispatchers.IO) { ApkAnalyzer.analyze(ctx, uri) }
                busy = false
                if (r == null) Toaster.show("Not a valid APK or it couldn't be read", ToastType.Error)
                onResult(r)
            }
        }
    }
    GlowButton(if (busy) "Analyzing..." else buttonText, "📦", true, {
        if (!busy) picker.launch(arrayOf("*/*"))
    }, Modifier.fillMaxWidth())
}

/* 1. APK Inspector */
@Composable
fun ApkInspectorTool(onBack: () -> Unit) {
    var report by remember { mutableStateOf<ApkReport?>(null) }
    ToolScaffold("APK Inspector", "📦", onBack) {
        InfoNote("Select an APK to read its package name, version, SDK levels, size and signing certificate. The file stays on your device and is never installed or changed.")
        GapV()
        ApkPicker("Select APK") { report = it }
        report?.let { r ->
            GapV()
            GlassCard {
                ResultRow("App name", r.label)
                ResultRow("Package", r.pkg)
                ResultRow("Version", r.versionName + " (" + r.versionCode + ")")
                ResultRow("Min SDK", r.minSdk.toString())
                ResultRow("Target SDK", r.targetSdk.toString())
                ResultRow("File size", formatBytes(r.sizeBytes))
                ResultRow("Permissions", r.permissions.size.toString())
                ResultRow("File SHA-256", r.sha256)
                r.signers.forEachIndexed { i, s -> ResultRow("Signer ${i + 1} (SHA-256)", s) }
            }
        }
    }
}

/* 2. Permission scanner */
private val HIGH_PERMS = setOf(
    "READ_SMS", "SEND_SMS", "RECEIVE_SMS", "READ_CALL_LOG", "WRITE_CALL_LOG", "PROCESS_OUTGOING_CALLS",
    "CALL_PHONE", "RECORD_AUDIO", "READ_CONTACTS", "WRITE_CONTACTS", "SYSTEM_ALERT_WINDOW",
    "REQUEST_INSTALL_PACKAGES", "BIND_ACCESSIBILITY_SERVICE", "BIND_DEVICE_ADMIN", "MANAGE_EXTERNAL_STORAGE",
    "QUERY_ALL_PACKAGES", "READ_PHONE_NUMBERS", "ACCESS_BACKGROUND_LOCATION",
    "BIND_NOTIFICATION_LISTENER_SERVICE", "WRITE_SETTINGS", "INSTALL_PACKAGES", "DELETE_PACKAGES"
)
private val MEDIUM_PERMS = setOf(
    "CAMERA", "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION", "READ_PHONE_STATE", "WRITE_EXTERNAL_STORAGE",
    "READ_EXTERNAL_STORAGE", "GET_ACCOUNTS", "READ_CALENDAR", "WRITE_CALENDAR", "BODY_SENSORS",
    "BLUETOOTH_CONNECT", "BLUETOOTH_SCAN", "READ_MEDIA_IMAGES", "READ_MEDIA_VIDEO", "READ_MEDIA_AUDIO",
    "RECEIVE_BOOT_COMPLETED"
)

@Composable
fun PermissionScannerTool(onBack: () -> Unit) {
    var report by remember { mutableStateOf<ApkReport?>(null) }
    ToolScaffold("Permission Scanner", "🛡️", onBack) {
        InfoNote("Checks which sensitive permissions an APK asks for. Many legitimate apps need some of them, so use this as a guide, not a verdict.")
        GapV()
        ApkPicker("Select APK") { report = it }
        report?.let { r ->
            val names = r.permissions.map { it.substringAfterLast('.') }
            val high = names.filter { it in HIGH_PERMS }
            val medium = names.filter { it in MEDIUM_PERMS }
            val level = when {
                high.size >= 3 -> "High"
                high.isNotEmpty() || medium.size >= 4 -> "Medium"
                else -> "Low"
            }
            val color = when (level) {
                "High" -> MColors.Danger
                "Medium" -> MColors.Warn
                else -> MColors.Lime
            }
            GapV()
            GlassCard {
                MText(r.label + "  (" + r.pkg + ")", size = 13.sp, weight = FontWeight.SemiBold, maxLines = 2)
                GapV(8)
                MText("Risk level: $level", size = 18.sp, weight = FontWeight.Bold, color = color)
                MText(
                    "${r.permissions.size} permissions · ${high.size} high · ${medium.size} medium",
                    size = 11.sp, color = MColors.Muted
                )
            }
            if (high.isNotEmpty()) {
                SectionTitle("High risk")
                GlassCard { high.forEach { MText("🔴 $it", size = 12.sp, modifier = Modifier.padding(vertical = 3.dp)) } }
            }
            if (medium.isNotEmpty()) {
                SectionTitle("Medium risk")
                GlassCard { medium.forEach { MText("🟠 $it", size = 12.sp, modifier = Modifier.padding(vertical = 3.dp)) } }
            }
            val others = names.filter { it !in HIGH_PERMS && it !in MEDIUM_PERMS }
            if (others.isNotEmpty()) {
                SectionTitle("Other (${others.size})")
                GlassCard { others.forEach { MText("⚪ $it", size = 11.sp, color = MColors.Muted, modifier = Modifier.padding(vertical = 2.dp)) } }
            }
        }
    }
}

/* 3. Signature compare */
@Composable
fun SignatureCompareTool(onBack: () -> Unit) {
    var a by remember { mutableStateOf<ApkReport?>(null) }
    var b by remember { mutableStateOf<ApkReport?>(null) }
    ToolScaffold("Signature Compare", "✍️", onBack) {
        InfoNote("Select two APKs to see whether they were signed with the same certificate. A different signer means the file was re-signed by someone else.")
        GapV()
        MText("APK A (original)", size = 12.sp, color = MColors.Muted)
        GapV(6)
        ApkPicker("Select APK A") { a = it }
        a?.let { MText(it.label + " · " + it.versionName, size = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
        GapV()
        MText("APK B (to check)", size = 12.sp, color = MColors.Muted)
        GapV(6)
        ApkPicker("Select APK B") { b = it }
        b?.let { MText(it.label + " · " + it.versionName, size = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
        val ra = a
        val rb = b
        if (ra != null && rb != null) {
            val same = ra.signers.isNotEmpty() && ra.signers.toSet().intersect(rb.signers.toSet()).isNotEmpty()
            GapV()
            GlassCard {
                MText(
                    if (same) "✅ Same signing certificate" else "⚠️ Different signing certificates",
                    size = 15.sp, weight = FontWeight.Bold, color = if (same) MColors.Lime else MColors.Warn
                )
                GapV(8)
                ResultRow("A signer", ra.signers.firstOrNull() ?: "none")
                ResultRow("B signer", rb.signers.firstOrNull() ?: "none")
                ResultRow("Same package name", if (ra.pkg == rb.pkg) "Yes" else "No (${ra.pkg} / ${rb.pkg})")
            }
        }
    }
}

/* 4. Version comparator */
@Composable
fun VersionCompareTool(onBack: () -> Unit) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    ToolScaffold("Version Comparator", "🔢", onBack) {
        ToolInput(a, { a = it }, "Version A (e.g. 1.9.4)")
        GapV(10)
        ToolInput(b, { b = it }, "Version B (e.g. v2.0)")
        if (a.isNotBlank() && b.isNotBlank()) {
            val c = compareVersions(a, b)
            GapV()
            GlassCard {
                MText(
                    when {
                        c > 0 -> "A is newer than B"
                        c < 0 -> "B is newer than A"
                        else -> "Both versions are equal"
                    },
                    size = 16.sp, weight = FontWeight.Bold, color = MColors.Lime
                )
            }
        }
    }
}

/* 5. Base64 */
@Composable
fun Base64Tool(onBack: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("Standard") }
    val flags = Base64.NO_WRAP or (if (mode == "URL-safe") Base64.URL_SAFE else 0)
    ToolScaffold("Base64 Tool", "🔤", onBack) {
        ChipRow(listOf("Standard", "URL-safe"), mode) { mode = it }
        GapV()
        ToolInput(input, { input = it }, "Input", minLines = 4, mono = true)
        GapV()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlowButton("Encode", "⬆️", true, {
                output = Base64.encodeToString(input.toByteArray(), flags)
            }, Modifier.weight(1f))
            GlowButton("Decode", "⬇️", false, {
                output = try {
                    String(Base64.decode(input.trim(), flags))
                } catch (e: IllegalArgumentException) {
                    Toaster.show("Not valid Base64", ToastType.Error)
                    ""
                }
            }, Modifier.weight(1f))
        }
        if (output.isNotEmpty()) {
            GapV()
            GlassCard { ResultRow("Result", output) }
        }
    }
}

/* 6. Text hash */
@Composable
fun TextHashTool(onBack: () -> Unit) {
    var input by remember { mutableStateOf("") }
    ToolScaffold("Text Hash", "#️⃣", onBack) {
        ToolInput(input, { input = it }, "Text", minLines = 3, mono = true)
        if (input.isNotEmpty()) {
            GapV()
            val bytes = input.toByteArray()
            GlassCard {
                ResultRow("MD5", digestHex("MD5", bytes))
                ResultRow("SHA-1", digestHex("SHA-1", bytes))
                ResultRow("SHA-256", digestHex("SHA-256", bytes))
                ResultRow("SHA-512", digestHex("SHA-512", bytes))
            }
        }
    }
}

/* 7. Number base converter */
private fun convertNumber(mode: String, input: String): List<Pair<String, String>> {
    val t = input.trim()
    if (t.isEmpty()) return emptyList()
    return try {
        if (mode == "TEXT") {
            val b = t.toByteArray()
            listOf(
                "HEX" to b.joinToString(" ") { "%02X".format(it) },
                "BIN" to b.joinToString(" ") { Integer.toBinaryString(it.toInt() and 0xFF).padStart(8, '0') },
                "DEC" to b.joinToString(" ") { (it.toInt() and 0xFF).toString() }
            )
        } else {
            val radix = when (mode) {
                "HEX" -> 16
                "BIN" -> 2
                "OCT" -> 8
                else -> 10
            }
            val v = BigInteger(t.removePrefix("0x").removePrefix("0X"), radix)
            val rows = mutableListOf(
                "DEC" to v.toString(10),
                "HEX" to v.toString(16).uppercase(),
                "BIN" to v.toString(2),
                "OCT" to v.toString(8)
            )
            if (v.signum() >= 0 && v.bitLength() <= 16 && v.toInt() in 32..126) {
                rows.add("ASCII" to v.toInt().toChar().toString())
            }
            rows
        }
    } catch (e: Exception) {
        listOf("Error" to "Invalid $mode value")
    }
}

@Composable
fun BaseConverterTool(onBack: () -> Unit) {
    var mode by remember { mutableStateOf("DEC") }
    var input by remember { mutableStateOf("") }
    val rows = remember(mode, input) { convertNumber(mode, input) }
    ToolScaffold("Hex / Bin / Dec", "🧮", onBack) {
        ChipRow(listOf("DEC", "HEX", "BIN", "OCT", "TEXT"), mode) { mode = it }
        GapV()
        ToolInput(input, { input = it }, "Input ($mode)", mono = true, minLines = 2)
        if (rows.isNotEmpty()) {
            GapV()
            GlassCard { rows.forEach { (k, v) -> ResultRow(k, v) } }
        }
    }
}

/* 8. Color converter */
private fun parseColorInput(s: String): Int? {
    var t = s.trim().removePrefix("#").removePrefix("0x").removePrefix("0X")
    if (t.length == 3) t = t.map { "$it$it" }.joinToString("")
    if (t.length == 6) t = "FF$t"
    if (t.length != 8) return null
    return t.toLongOrNull(16)?.toInt()
}

@Composable
fun ColorConverterTool(onBack: () -> Unit) {
    var input by remember { mutableStateOf("#3FD11F") }
    val c = parseColorInput(input)
    ToolScaffold("Color Converter", "🎨", onBack) {
        ToolInput(input, { input = it }, "Color (#RGB, #RRGGBB or #AARRGGBB)", mono = true)
        GapV()
        if (c == null) {
            MText("Enter a valid hex color.", size = 12.sp, color = MColors.Muted)
        } else {
            val a = (c ushr 24) and 0xFF
            val r = (c ushr 16) and 0xFF
            val g = (c ushr 8) and 0xFF
            val bl = c and 0xFF
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(c, hsv)
            val hex = String.format("%08X", c)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(c))
                    .border(1.dp, MColors.Muted.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            )
            GapV()
            GlassCard {
                ResultRow("Android XML / HEX", "#$hex")
                ResultRow("Kotlin Compose", "Color(0x$hex)")
                ResultRow("Android Color.parseColor", "Color.parseColor(\"#$hex\")")
                ResultRow("RGB", "$r, $g, $bl")
                ResultRow("Alpha", "$a (${a * 100 / 255}%)")
                ResultRow("HSV", String.format("%.0f°, %.0f%%, %.0f%%", hsv[0], hsv[1] * 100, hsv[2] * 100))
                ResultRow("Signed int", c.toString())
            }
        }
    }
}

/* 9. dp / px converter */
@Composable
fun DpPxTool(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val dm = ctx.resources.displayMetrics
    val dens = remember {
        listOf(
            "Device" to dm.density,
            "mdpi" to 1f,
            "hdpi" to 1.5f,
            "xhdpi" to 2f,
            "xxhdpi" to 3f,
            "xxxhdpi" to 4f
        )
    }
    var sel by remember { mutableStateOf("Device") }
    var input by remember { mutableStateOf("16") }
    val d = dens.first { it.first == sel }.second
    val v = input.toFloatOrNull()
    ToolScaffold("dp / px Converter", "📐", onBack) {
        ChipRow(dens.map { it.first }, sel) { sel = it }
        GapV(6)
        MText("Density: $d  (" + (d * 160).toInt() + " dpi)", size = 11.sp, color = MColors.Muted)
        GapV()
        ToolInput(input, { input = it }, "Value")
        if (v != null) {
            GapV()
            GlassCard {
                ResultRow("$v dp  →  px", String.format("%.1f px", v * d))
                ResultRow("$v px  →  dp", String.format("%.1f dp", v / d))
                ResultRow("$v sp  →  px", String.format("%.1f px", v * d * ctx.resources.configuration.fontScale))
            }
            SectionTitle("$v dp on every density")
            GlassCard {
                dens.drop(1).forEach { (name, density) -> ResultRow(name, String.format("%.1f px", v * density)) }
            }
        }
    }
}

/* 10. JSON formatter */
@Composable
fun JsonFormatterTool(onBack: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    fun run(pretty: Boolean) {
        val t = input.trim()
        if (t.isEmpty()) return
        try {
            val text = if (t.startsWith("[")) {
                val arr = JSONArray(t)
                if (pretty) arr.toString(2) else arr.toString()
            } else {
                val obj = JSONObject(t)
                if (pretty) obj.toString(2) else obj.toString()
            }
            output = text
            error = ""
        } catch (e: Exception) {
            output = ""
            error = e.message ?: "Invalid JSON"
        }
    }

    ToolScaffold("JSON Formatter", "🧾", onBack) {
        ToolInput(input, { input = it }, "JSON", minLines = 6, mono = true)
        GapV()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlowButton("Format", "✨", true, { run(true) }, Modifier.weight(1f))
            GlowButton("Minify", "📦", false, { run(false) }, Modifier.weight(1f))
        }
        if (error.isNotEmpty()) {
            GapV()
            MText("❌ $error", size = 12.sp, color = MColors.Danger)
        }
        if (output.isNotEmpty()) {
            GapV()
            GlassCard {
                MText(output, size = 11.sp, lineHeight = 16.sp)
                GapV(10)
                val ctx = LocalContext.current
                GlowButton("Copy", "📋", false, { copyText(ctx, "JSON", output) }, Modifier.fillMaxWidth())
            }
        }
    }
}

/* 11. API level cheat sheet */
private val ApiLevels = listOf(
    Triple(36, "Android 16", "Baklava"),
    Triple(35, "Android 15", "Vanilla Ice Cream"),
    Triple(34, "Android 14", "Upside Down Cake"),
    Triple(33, "Android 13", "Tiramisu"),
    Triple(32, "Android 12L", "Snow Cone v2"),
    Triple(31, "Android 12", "Snow Cone"),
    Triple(30, "Android 11", "Red Velvet Cake"),
    Triple(29, "Android 10", "Quince Tart"),
    Triple(28, "Android 9", "Pie"),
    Triple(27, "Android 8.1", "Oreo"),
    Triple(26, "Android 8.0", "Oreo"),
    Triple(25, "Android 7.1", "Nougat"),
    Triple(24, "Android 7.0", "Nougat"),
    Triple(23, "Android 6.0", "Marshmallow"),
    Triple(22, "Android 5.1", "Lollipop"),
    Triple(21, "Android 5.0", "Lollipop")
)

@Composable
fun ApiLevelsTool(onBack: () -> Unit) {
    ToolScaffold("API Level Guide", "📊", onBack) {
        InfoNote("Your device runs API ${Build.VERSION.SDK_INT}. Use this table when choosing minSdk / targetSdk or reading an APK's requirements.")
        GapV()
        GlassCard {
            ApiLevels.forEach { (api, name, code) ->
                val mine = api == Build.VERSION.SDK_INT
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MText("API $api", size = 12.sp, weight = FontWeight.Bold, color = if (mine) MColors.Lime else MColors.TextPrimary, modifier = Modifier.width(70.dp))
                    MText(name, size = 12.sp, weight = FontWeight.Medium, modifier = Modifier.width(100.dp))
                    MText(code + if (mine) "  ← you" else "", size = 11.sp, color = MColors.Muted)
                }
            }
        }
    }
}

/* 12. ADB / build command cheat sheet */
private val AdbCommands = listOf(
    "List connected devices" to "adb devices",
    "Install an APK" to "adb install -r app.apk",
    "Install (allow downgrade)" to "adb install -r -d app.apk",
    "Uninstall an app" to "adb uninstall com.example.app",
    "List third-party packages" to "adb shell pm list packages -3",
    "Show package details" to "adb shell dumpsys package com.example.app",
    "Clear app data" to "adb shell pm clear com.example.app",
    "Copy file from device" to "adb pull /sdcard/file.txt .",
    "Copy file to device" to "adb push file.txt /sdcard/",
    "Live log (filtered)" to "adb logcat -s MyTag",
    "Verify APK signature" to "apksigner verify --verbose app.apk",
    "Align an APK" to "zipalign -p 4 in.apk out.apk",
    "Show APK info" to "aapt dump badging app.apk",
    "Create a keystore" to "keytool -genkeypair -v -keystore my.jks -alias key -keyalg RSA -keysize 2048 -validity 10000"
)

@Composable
fun AdbCheatTool(onBack: () -> Unit) {
    val ctx = LocalContext.current
    ToolScaffold("ADB & Build Cheats", "💻", onBack) {
        InfoNote("Tap a command to copy it.")
        GapV()
        AdbCommands.forEach { (title, cmd) ->
            GlassCard(onClick = { copyText(ctx, "Command", cmd) }) {
                MText(title, size = 12.sp, weight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                MText(cmd, size = 11.sp, color = MColors.Lime, lineHeight = 16.sp)
            }
            GapV(10)
        }
    }
}
