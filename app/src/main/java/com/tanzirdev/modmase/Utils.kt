package com.tanzirdev.modmase

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import com.tanzirdev.modmase.ui.ToastType
import com.tanzirdev.modmase.ui.Toaster
import java.io.File

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
        Toaster.show("No app found to open this link", ToastType.Error)
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

/** Opens a post/app Telegram link (falls back to the channel). */
fun openLinkOrChannel(ctx: Context, link: String) {
    if (link.startsWith("https://") || link.startsWith("http://")) openUrl(ctx, link) else openTelegram(ctx)
}

fun copyText(ctx: Context, label: String, text: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
    Toaster.show("$label copied", ToastType.Success)
}

fun shareText(ctx: Context, text: String) {
    try {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(send, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(chooser)
    } catch (e: Exception) {
        Toaster.show("Unable to share", ToastType.Error)
    }
}

/** Shares bytes as a file (text report, QR image...) through FileProvider. */
fun shareFileBytes(ctx: Context, fileName: String, bytes: ByteArray, mime: String) {
    try {
        val dir = File(ctx.cacheDir, "shared")
        dir.mkdirs()
        val f = File(dir, fileName)
        f.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", f)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(chooser)
    } catch (e: Exception) {
        Toaster.show("Unable to share file", ToastType.Error)
    }
}

fun openSystemSettings(ctx: Context, action: String) {
    try {
        ctx.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toaster.show("This screen isn't available on your device", ToastType.Error)
    }
}
