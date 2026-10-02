package com.tanzirdev.modmase

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object Links {
    const val TELEGRAM = "https://t.me/MODMASE"
    const val TELEGRAM_APP = "tg://resolve?domain=MODMASE"
    const val WEBSITE = "https://modmase.vercel.app"
}

fun loadAssetBitmap(context: Context, path: String): ImageBitmap =
    context.assets.open(path).use { BitmapFactory.decodeStream(it).asImageBitmap() }

fun openUrl(ctx: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(ctx, "No app found to open this link", Toast.LENGTH_SHORT).show()
    }
}

fun openTelegram(ctx: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Links.TELEGRAM_APP)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        openUrl(ctx, Links.TELEGRAM)
    } catch (e: Exception) {
        openUrl(ctx, Links.TELEGRAM)
    }
}

fun copyText(ctx: Context, label: String, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(ctx, "$label copied", Toast.LENGTH_SHORT).show()
}

fun shareText(ctx: Context, text: String) {
    try {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(send, "Share MODMASE").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(ctx, "Unable to share", Toast.LENGTH_SHORT).show()
    }
}

fun openSystemSettings(ctx: Context, action: String) {
    try {
        ctx.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(ctx, "This screen isn't available on your device", Toast.LENGTH_SHORT).show()
    }
}
