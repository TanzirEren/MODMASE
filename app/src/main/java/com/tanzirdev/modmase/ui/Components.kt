package com.tanzirdev.modmase.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.tanzirdev.modmase.Prefs
import kotlinx.coroutines.delay

/* ---------- Text ---------- */

@Composable
fun MText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 14.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = MColors.TextPrimary,
    script: Boolean = false,
    align: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) {
    val fonts = LocalFonts.current
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        fontFamily = if (script) fonts.script else fonts.body,
        textAlign = align,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(modifier.padding(top = 26.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(MColors.Lime, MColors.DeepGreen)))
        )
        Spacer(Modifier.width(10.dp))
        MText(text, size = 17.sp, weight = FontWeight.SemiBold)
    }
}

/* ---------- Haptics ---------- */

@Composable
fun rememberHaptic(): () -> Unit {
    val h = LocalHapticFeedback.current
    return remember(h) {
        {
            if (Prefs.haptics) h.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

/* ---------- Layout helpers ---------- */

@Composable
fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 120.dp),
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = rememberHaptic()
    val shape = RoundedCornerShape(22.dp)
    val base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(Brush.linearGradient(listOf(MColors.SurfaceHigh, MColors.Surface)))
        .border(1.dp, MColors.Green.copy(alpha = 0.18f), shape)
    val finalModifier = if (onClick != null) {
        base.clickable {
            haptic()
            onClick()
        }
    } else {
        base
    }
    Column(finalModifier.padding(18.dp), content = content)
}

/** Fades + slides a block in with a small stagger. */
@Composable
fun Reveal(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(70L * index)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 6 }
    ) {
        content()
    }
}

/* ---------- Buttons / chips ---------- */

@Composable
fun GlowButton(
    text: String,
    emoji: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHaptic()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(),
        label = "btnScale"
    )
    val shape = RoundedCornerShape(18.dp)
    val look = if (filled) {
        Modifier.background(Brush.horizontalGradient(listOf(MColors.DeepGreen, MColors.Green, MColors.Lime)))
    } else {
        Modifier
            .background(MColors.Surface)
            .border(1.2.dp, MColors.Green.copy(alpha = 0.65f), shape)
    }
    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .then(look)
            .clickable(interactionSource = interaction, indication = null) {
                haptic()
                onClick()
            }
            .padding(vertical = 15.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji.isNotEmpty()) {
                MText(emoji, size = 16.sp)
                Spacer(Modifier.width(8.dp))
            }
            MText(
                text = text,
                size = 14.sp,
                weight = FontWeight.SemiBold,
                color = if (filled) Color.Black else MColors.TextPrimary
            )
        }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val haptic = rememberHaptic()
    val bg by animateColorAsState(
        targetValue = if (selected) MColors.Green.copy(alpha = 0.22f) else MColors.Surface,
        label = "chipBg"
    )
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .clip(shape)
            .background(bg)
            .border(
                1.dp,
                if (selected) MColors.Lime.copy(alpha = 0.7f) else MColors.Green.copy(alpha = 0.2f),
                shape
            )
            .clickable {
                haptic()
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        MText(
            text,
            size = 12.sp,
            weight = FontWeight.SemiBold,
            color = if (selected) MColors.Lime else MColors.Muted
        )
    }
}

@Composable
fun Tag(text: String, color: Color = MColors.Lime) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        MText(text, size = 10.sp, weight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun SettingSwitch(title: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val haptic = rememberHaptic()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            MText(title, size = 14.sp, weight = FontWeight.Medium)
            if (desc.isNotEmpty()) MText(desc, size = 11.sp, color = MColors.Muted)
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                haptic()
                onChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = MColors.Lime,
                uncheckedThumbColor = MColors.Muted,
                uncheckedTrackColor = MColors.Surface
            )
        )
    }
}

/* ---------- Screen header (back button) ---------- */

@Composable
fun ScreenHeader(title: String, emoji: String, onBack: () -> Unit) {
    val haptic = rememberHaptic()
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MColors.Surface)
                .border(1.dp, MColors.Green.copy(alpha = 0.3f), CircleShape)
                .clickable {
                    haptic()
                    onBack()
                },
            contentAlignment = Alignment.Center
        ) {
            MText("←", size = 18.sp, weight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        if (emoji.isNotEmpty()) {
            MText(emoji, size = 20.sp)
            Spacer(Modifier.width(8.dp))
        }
        MText(title, size = 18.sp, weight = FontWeight.Bold, maxLines = 1)
    }
}

/* ---------- Background ---------- */

private class Particle(val x: Float, val y: Float, val r: Float, val speed: Float)

@Composable
fun AuroraBackground() {
    val transition = rememberInfiniteTransition(label = "aurora")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )
    val rise by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart),
        label = "rise"
    )
    val particles = remember {
        List(26) { i ->
            Particle(
                x = ((i * 37) % 100) / 100f,
                y = ((i * 53) % 100) / 100f,
                r = 1.2f + (i % 4) * 0.9f,
                speed = 0.4f + (i % 5) * 0.2f
            )
        }
    }
    val green = MColors.Green
    val lime = MColors.Lime
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(MColors.Bg)
        drawRect(
            Brush.radialGradient(
                colors = listOf(green.copy(alpha = 0.30f), Color.Transparent),
                center = Offset(w * (0.10f + 0.50f * drift), h * 0.08f),
                radius = w * 0.9f
            )
        )
        drawRect(
            Brush.radialGradient(
                colors = listOf(lime.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(w * (0.95f - 0.45f * drift), h * 0.85f),
                radius = w * 0.8f
            )
        )
        particles.forEach { p ->
            val yy = (((p.y - rise * p.speed) % 1f) + 1f) % 1f
            drawCircle(
                color = lime.copy(alpha = 0.35f),
                radius = p.r.dp.toPx(),
                center = Offset(p.x * w, yy * h)
            )
        }
    }
}

/* ---------- Banner + badges ---------- */

@Composable
fun BannerCard(bitmap: ImageBitmap) {
    val transition = rememberInfiniteTransition(label = "banner")
    val glow by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val bob by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val shape = RoundedCornerShape(26.dp)
    Image(
        bitmap = bitmap,
        contentDescription = "MODMASE banner",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2.5f)
            .offset(y = bob.dp)
            .shadow(
                elevation = 22.dp,
                shape = shape,
                ambientColor = MColors.Green.copy(alpha = glow),
                spotColor = MColors.Green.copy(alpha = glow)
            )
            .clip(shape)
            .border(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        MColors.Lime.copy(alpha = glow),
                        MColors.Green.copy(alpha = 0.2f),
                        MColors.Lime.copy(alpha = glow)
                    )
                ),
                shape
            )
    )
}

@Composable
fun RoundIcon(icon: ImageBitmap, size: Int = 52) {
    Image(
        bitmap = icon,
        contentDescription = "MODMASE icon",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(2.dp, Brush.linearGradient(listOf(MColors.Lime, MColors.DeepGreen)), CircleShape)
    )
}

@Composable
fun LiveBadge() {
    val transition = rememberInfiniteTransition(label = "live")
    val a by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "liveAlpha"
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MColors.Green.copy(alpha = 0.14f))
            .border(1.dp, MColors.Green.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MColors.Lime.copy(alpha = a))
        )
        Spacer(Modifier.width(7.dp))
        MText("LIVE", size = 11.sp, weight = FontWeight.Bold, color = MColors.Lime, letterSpacing = 1.sp)
    }
}

/* ---------- Shimmer ---------- */

@Composable
fun shimmerBrush(): Brush {
    val t = rememberInfiniteTransition(label = "shimmer")
    val x by t.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX"
    )
    return Brush.linearGradient(
        colors = listOf(MColors.Surface, MColors.SurfaceHigh, MColors.Surface),
        start = Offset(x - 400f, 0f),
        end = Offset(x, 300f)
    )
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, radius: Int = 16) {
    Box(
        modifier
            .clip(RoundedCornerShape(radius.dp))
            .background(shimmerBrush())
    )
}

@Composable
fun PostSkeleton() {
    GlassCard {
        ShimmerBox(Modifier.fillMaxWidth().aspectRatio(16f / 9f))
        Spacer(Modifier.height(14.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.5f).height(14.dp), 7)
        Spacer(Modifier.height(10.dp))
        ShimmerBox(Modifier.fillMaxWidth().height(12.dp), 6)
        Spacer(Modifier.height(6.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.8f).height(12.dp), 6)
    }
}

@Composable
fun AppRowSkeleton() {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShimmerBox(Modifier.size(56.dp), 14)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                ShimmerBox(Modifier.fillMaxWidth(0.6f).height(14.dp), 7)
                Spacer(Modifier.height(8.dp))
                ShimmerBox(Modifier.fillMaxWidth(0.9f).height(11.dp), 6)
            }
        }
    }
}

/* ---------- Lottie ---------- */

@Composable
fun LottieView(asset: String, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(asset))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieView("lottie/empty.json", Modifier.size(170.dp))
        MText(title, size = 16.sp, weight = FontWeight.SemiBold, align = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        MText(subtitle, size = 12.sp, color = MColors.Muted, align = TextAlign.Center)
    }
}

@Composable
fun ErrorState(onRetry: () -> Unit) {
    GlassCard {
        MText("⚠️ Couldn't load updates", size = 15.sp, weight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        MText("Check your internet connection and try again.", size = 12.sp, color = MColors.Muted)
        Spacer(Modifier.height(14.dp))
        GlowButton("Retry", "🔄", true, onRetry, Modifier.fillMaxWidth())
    }
}

/* ---------- Base64 images (from the admin panel) ---------- */

fun decodeDataUri(data: String): ImageBitmap? {
    return try {
        if (data.isBlank()) {
            null
        } else {
            val b64 = data.substringAfter("base64,", data)
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    } catch (e: Throwable) {
        null
    }
}

@Composable
fun rememberDataImage(data: String): ImageBitmap? = remember(data) { decodeDataUri(data) }

/* ---------- Animated toast ---------- */

enum class ToastType(val emoji: String) {
    Success("✅"),
    Info("💡"),
    Error("⚠️")
}

class ToastData(val id: Long, val message: String, val type: ToastType)

object Toaster {
    var current by mutableStateOf<ToastData?>(null)
        private set

    fun show(message: String, type: ToastType = ToastType.Info) {
        current = ToastData(System.nanoTime(), message, type)
    }

    fun dismiss() {
        current = null
    }
}

@Composable
fun ToastHost() {
    val t = Toaster.current
    val last = remember { mutableStateOf<ToastData?>(null) }
    LaunchedEffect(t?.id) {
        if (t != null) {
            last.value = t
            delay(2600)
            if (Toaster.current?.id == t.id) Toaster.dismiss()
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 10.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = t != null,
            enter = slideInVertically(spring()) { -it * 2 } + fadeIn() + scaleIn(initialScale = 0.85f),
            exit = slideOutVertically(tween(250)) { -it * 2 } + fadeOut(tween(250)) + scaleOut(targetScale = 0.9f)
        ) {
            val data = last.value
            if (data != null) {
                val accent = when (data.type) {
                    ToastType.Success -> MColors.Lime
                    ToastType.Info -> MColors.Green
                    ToastType.Error -> MColors.Danger
                }
                val shape = RoundedCornerShape(20.dp)
                Row(
                    Modifier
                        .shadow(18.dp, shape, ambientColor = accent, spotColor = accent)
                        .clip(shape)
                        .background(Brush.horizontalGradient(listOf(MColors.SurfaceHigh, MColors.Surface)))
                        .border(1.2.dp, accent.copy(alpha = 0.7f), shape)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        MText(data.type.emoji, size = 16.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    MText(data.message, size = 13.sp, weight = FontWeight.Medium, maxLines = 3)
                }
            }
        }
    }
}
