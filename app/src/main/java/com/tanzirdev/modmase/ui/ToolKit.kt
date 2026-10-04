package com.tanzirdev.modmase.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanzirdev.modmase.copyText
import java.security.MessageDigest

/* ---------- helpers ---------- */

fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

fun digestHex(algo: String, data: ByteArray): String =
    MessageDigest.getInstance(algo).digest(data).toHex()

fun fileInfo(ctx: Context, uri: Uri): Pair<String, Long> {
    var name = "file"
    var size = 0L
    try {
        ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val ni = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val si = c.getColumnIndex(OpenableColumns.SIZE)
                if (ni >= 0) name = c.getString(ni) ?: "file"
                if (si >= 0) size = c.getLong(si)
            }
        }
    } catch (e: Exception) {
        // keep defaults
    }
    return Pair(name, size)
}

fun formatBytes(b: Long): String {
    if (b < 1024) return "$b B"
    val kb = b / 1024.0
    if (kb < 1024) return String.format(java.util.Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(java.util.Locale.US, "%.1f MB", mb)
    return String.format(java.util.Locale.US, "%.2f GB", mb / 1024.0)
}

/* ---------- layout ---------- */

@Composable
fun ToolScaffold(
    title: String,
    emoji: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title, emoji, onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            content = content
        )
    }
}

@Composable
fun ToolInput(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    mono: Boolean = false
) {
    Column(modifier.fillMaxWidth()) {
        MText(label, size = 12.sp, color = MColors.Muted)
        Spacer(Modifier.height(6.dp))
        val shape = RoundedCornerShape(16.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MColors.Surface)
                .border(1.dp, MColors.Green.copy(alpha = 0.25f), shape)
                .padding(14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                minLines = minLines,
                textStyle = TextStyle(
                    color = MColors.TextPrimary,
                    fontSize = 14.sp,
                    fontFamily = if (mono) FontFamily.Monospace else LocalFonts.current.body
                ),
                cursorBrush = SolidColor(MColors.Lime),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    val ctx = LocalContext.current
    val haptic = rememberHaptic()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f)) {
            MText(label, size = 11.sp, color = MColors.Muted)
            Spacer(Modifier.height(2.dp))
            MText(value.ifEmpty { "-" }, size = 12.sp, lineHeight = 17.sp, weight = FontWeight.Medium)
        }
        if (value.isNotEmpty()) {
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MColors.Green.copy(alpha = 0.15f))
                    .clickable {
                        haptic()
                        copyText(ctx, label, value)
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                MText("Copy", size = 10.sp, color = MColors.Lime, weight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ProgressBar(progress: Float) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(MColors.Bg)
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(MColors.DeepGreen, MColors.Green, MColors.Lime)
                    )
                )
        )
    }
}

@Composable
fun InfoNote(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MColors.Green.copy(alpha = 0.08f))
            .border(1.dp, MColors.Green.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        MText(text, size = 11.sp, color = MColors.Muted, lineHeight = 16.sp, align = TextAlign.Start)
    }
}

@Composable
fun GapV(h: Int = 14) {
    Spacer(Modifier.height(h.dp))
}

@Composable
fun ChipRow(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { Chip(it, it == selected) { onSelect(it) } }
    }
}
