package com.tanzirdev.modmase.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    letterSpacing: TextUnit = TextUnit.Unspecified
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
        letterSpacing = letterSpacing
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
    val shape = RoundedCornerShape(22.dp)
    val base = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(Brush.linearGradient(listOf(MColors.SurfaceHigh, MColors.Surface)))
        .border(1.dp, MColors.Green.copy(alpha = 0.18f), shape)
    val finalModifier = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(finalModifier.padding(18.dp), content = content)
}

/** Fades + slides a block in with a small stagger. */
@Composable
fun Reveal(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(90L * index)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(550)) + slideInVertically(tween(550)) { it / 6 }
    ) {
        content()
    }
}

/* ---------- Buttons ---------- */

@Composable
fun GlowButton(
    text: String,
    emoji: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MText(emoji, size = 16.sp)
            Spacer(Modifier.width(8.dp))
            MText(
                text = text,
                size = 14.sp,
                weight = FontWeight.SemiBold,
                color = if (filled) Color.Black else MColors.TextPrimary
            )
        }
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
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(MColors.Bg)
        drawRect(
            Brush.radialGradient(
                colors = listOf(MColors.Green.copy(alpha = 0.30f), Color.Transparent),
                center = Offset(w * (0.10f + 0.50f * drift), h * 0.08f),
                radius = w * 0.9f
            )
        )
        drawRect(
            Brush.radialGradient(
                colors = listOf(MColors.Lime.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(w * (0.95f - 0.45f * drift), h * 0.85f),
                radius = w * 0.8f
            )
        )
        particles.forEach { p ->
            val yy = (((p.y - rise * p.speed) % 1f) + 1f) % 1f
            drawCircle(
                color = MColors.Lime.copy(alpha = 0.35f),
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
