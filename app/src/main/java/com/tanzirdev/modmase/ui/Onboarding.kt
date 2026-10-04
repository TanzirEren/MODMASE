package com.tanzirdev.modmase.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanzirdev.modmase.Prefs
import com.tanzirdev.modmase.Push
import kotlinx.coroutines.launch

/* =====================================================================
 *  SPLASH (Lottie)
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
        LottieView("lottie/splash.json", Modifier.size(380.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = sc.value
                scaleY = sc.value
                alpha = al.value
            }
        ) {
            RoundIcon(icon, size = 112)
            Spacer(Modifier.height(20.dp))
            MText("MODMASE", size = 30.sp, weight = FontWeight.Bold, letterSpacing = 5.sp)
            Spacer(Modifier.height(4.dp))
            MText("Mod Better, Live Smarter", script = true, size = 17.sp, color = MColors.Lime)
        }
    }
}

/* =====================================================================
 *  ONBOARDING (first launch, 3 swipeable slides)
 * ===================================================================== */

private class OnbPage(val emoji: String, val title: String, val text: String)

@Composable
fun OnboardingScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = rememberHaptic()
    val pages = remember {
        listOf(
            OnbPage(
                "🔧", "Discover MODs & Apps",
                "Browse a growing library of Android MODs, apps, games and tools with full details and safe download links."
            ),
            OnbPage(
                "🔔", "Never miss a release",
                "A live updates feed and instant push notifications the moment something new drops on MODMASE."
            ),
            OnbPage(
                "🧰", "Mod smarter",
                "Hash checker, APK inspector, QR generator and more modder tools. Everything works offline."
            )
        )
    }
    val pager = rememberPagerState(pageCount = { pages.size })

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        Prefs.setPush(granted)
        Push.syncTopics()
        Prefs.setOnboarded(true)
    }

    fun finish() {
        if (Build.VERSION.SDK_INT >= 33 && !Push.notificationsAllowed(ctx)) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            Prefs.setPush(true)
            Push.syncTopics()
            Prefs.setOnboarded(true)
        }
    }

    val pulse = rememberInfiniteTransition(label = "onbPulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "onbScale"
    )

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (pager.currentPage < pages.size - 1) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            haptic()
                            Prefs.setOnboarded(true)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    MText("Skip", size = 13.sp, color = MColors.Muted, weight = FontWeight.Medium)
                }
            } else {
                Spacer(Modifier.height(34.dp))
            }
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
            val p = pages[page]
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    Modifier
                        .size(190.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(MColors.Green.copy(alpha = 0.35f), MColors.Surface)
                            )
                        )
                        .border(2.dp, Brush.linearGradient(listOf(MColors.Lime, MColors.DeepGreen)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    MText(p.emoji, size = 78.sp)
                }
                Spacer(Modifier.height(36.dp))
                MText(p.title, size = 24.sp, weight = FontWeight.Bold, align = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                MText(
                    p.text,
                    size = 14.sp,
                    color = MColors.Muted,
                    align = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in pages.indices) {
                val w by animateDpAsState(
                    targetValue = if (pager.currentPage == i) 28.dp else 8.dp,
                    label = "dotWidth"
                )
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = w, height = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (pager.currentPage == i) MColors.Lime else MColors.Muted.copy(alpha = 0.4f))
                )
            }
        }

        val last = pager.currentPage == pages.size - 1
        GlowButton(
            text = if (last) "Get started" else "Next",
            emoji = if (last) "🚀" else "",
            filled = true,
            onClick = {
                if (last) {
                    finish()
                } else {
                    scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
