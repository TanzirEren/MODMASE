package com.tanzirdev.modmase.ui

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/** Colors sampled from the MODMASE banner: near-black + neon green. */
object MColors {
    val Bg = Color(0xFF050806)
    val Surface = Color(0xFF0E1410)
    val SurfaceHigh = Color(0xFF151D18)
    val Green = Color(0xFF3FD11F)
    val Lime = Color(0xFF8CFF2E)
    val DeepGreen = Color(0xFF1B8A2E)
    val TextPrimary = Color(0xFFF4F7F5)
    val Muted = Color(0xFF93A399)
}

class AppFonts(val body: FontFamily, val script: FontFamily)

val LocalFonts = staticCompositionLocalOf<AppFonts> { error("Fonts not provided") }

/** Fonts live in app/src/main/assets/fonts and are loaded from the AssetManager. */
fun loadFonts(context: Context): AppFonts {
    val am = context.assets
    val body = FontFamily(
        Font("fonts/Poppins-Regular.ttf", am, FontWeight.Normal),
        Font("fonts/Poppins-Medium.ttf", am, FontWeight.Medium),
        Font("fonts/Poppins-SemiBold.ttf", am, FontWeight.SemiBold),
        Font("fonts/Poppins-Bold.ttf", am, FontWeight.Bold)
    )
    val script = FontFamily(Font("fonts/Pacifico-Regular.ttf", am, FontWeight.Normal))
    return AppFonts(body, script)
}

@Composable
fun ModmaseTheme(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val fonts = remember { loadFonts(ctx) }
    CompositionLocalProvider(LocalFonts provides fonts) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = MColors.Green,
                onPrimary = Color.Black,
                background = MColors.Bg,
                onBackground = MColors.TextPrimary,
                surface = MColors.Surface,
                onSurface = MColors.TextPrimary
            ),
            content = content
        )
    }
}
