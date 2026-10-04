package com.tanzirdev.modmase

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tanzirdev.modmase.ui.AboutScreen
import com.tanzirdev.modmase.ui.AppDetailScreen
import com.tanzirdev.modmase.ui.AppsScreen
import com.tanzirdev.modmase.ui.AuroraBackground
import com.tanzirdev.modmase.ui.GlowButton
import com.tanzirdev.modmase.ui.HomeScreen
import com.tanzirdev.modmase.ui.MColors
import com.tanzirdev.modmase.ui.MText
import com.tanzirdev.modmase.ui.ModmaseTheme
import com.tanzirdev.modmase.ui.OnboardingScreen
import com.tanzirdev.modmase.ui.PostDetailScreen
import com.tanzirdev.modmase.ui.SplashScreen
import com.tanzirdev.modmase.ui.ToastHost
import com.tanzirdev.modmase.ui.ToolPage
import com.tanzirdev.modmase.ui.ToolsScreen
import com.tanzirdev.modmase.ui.rememberHaptic
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        Push.createChannel(this)
        if (Prefs.onboarded) Push.syncTopics()
        handleIntent(intent)
        setContent {
            ModmaseTheme {
                ModmaseApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(i: Intent?) {
        if (i == null) return
        val type = i.getStringExtra("type")
        val id = i.getStringExtra("id")
        val tab = i.getStringExtra("open_tab")
        if (!type.isNullOrBlank() && !id.isNullOrBlank()) AppState.setPending(type, id)
        if (!tab.isNullOrBlank()) AppState.pendingTab = tab
    }
}

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Apps("Apps", Icons.Filled.Search),
    Tools("Tools", Icons.Filled.Build),
    About("About", Icons.Filled.Info)
}

sealed class Overlay {
    data class ToolView(val id: String, val expected: String = "") : Overlay()
    data class AppView(val id: String) : Overlay()
    data class PostView(val id: String) : Overlay()
}

@Composable
fun ModmaseApp() {
    val ctx = LocalContext.current
    val banner = remember { loadAssetBitmap(ctx, "images/modmase_banner.jpg") }
    val icon = remember { loadAssetBitmap(ctx, "images/modmase_icon.png") }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2300)
        showSplash = false
    }

    Box(Modifier.fillMaxSize().background(MColors.Bg)) {
        AuroraBackground()
        Crossfade(targetState = showSplash, animationSpec = tween(500), label = "phase") { splash ->
            if (splash) {
                SplashScreen(icon)
            } else if (!Prefs.onboarded) {
                OnboardingScreen()
            } else {
                MainShell(banner, icon)
            }
        }
        ToastHost()
    }
}

@Composable
fun MainShell(banner: ImageBitmap, icon: ImageBitmap) {
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(Tab.Home) }
    var appsFilter by remember { mutableStateOf("all") }
    var overlay by remember { mutableStateOf<Overlay?>(null) }

    LaunchedEffect(Unit) {
        AppState.loadCache(ctx)
        AppState.refresh(ctx)
    }

    // notification / deep-link navigation
    LaunchedEffect(AppState.pendingType, AppState.pendingId, AppState.posts, AppState.apps) {
        val t = AppState.pendingType
        val id = AppState.pendingId
        if (t == "app" && AppState.apps.any { it.id == id }) {
            tab = Tab.Apps
            overlay = Overlay.AppView(id)
            AppState.clearPending()
        } else if (t == "post" && AppState.posts.any { it.id == id }) {
            overlay = Overlay.PostView(id)
            AppState.clearPending()
        }
    }
    // app icon shortcuts
    LaunchedEffect(AppState.pendingTab) {
        when (AppState.pendingTab) {
            "apps" -> tab = Tab.Apps
            "tools" -> tab = Tab.Tools
        }
        if (AppState.pendingTab.isNotEmpty()) AppState.pendingTab = ""
    }

    BackHandler(enabled = overlay != null) { overlay = null }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = tab,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "tabs"
        ) { current ->
            when (current) {
                Tab.Home -> HomeScreen(
                    banner = banner,
                    icon = icon,
                    onOpenCategory = { c ->
                        appsFilter = c
                        tab = Tab.Apps
                    },
                    onOpenPost = { p -> overlay = Overlay.PostView(p.id) }
                )
                Tab.Apps -> AppsScreen(appsFilter) { a -> overlay = Overlay.AppView(a.id) }
                Tab.Tools -> ToolsScreen { id -> overlay = Overlay.ToolView(id) }
                Tab.About -> AboutScreen(icon)
            }
        }

        FloatingNavBar(
            selected = tab,
            onSelect = {
                if (it == Tab.Apps && tab != Tab.Apps) appsFilter = "all"
                tab = it
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        AnimatedContent(
            targetState = overlay,
            transitionSpec = {
                (slideInHorizontally(tween(280)) { it / 4 } + fadeIn(tween(280))) togetherWith
                    (slideOutHorizontally(tween(220)) { it / 4 } + fadeOut(tween(220)))
            },
            label = "overlay"
        ) { o ->
            if (o != null) {
                Box(Modifier.fillMaxSize().background(MColors.Bg)) {
                    AuroraBackground()
                    when (o) {
                        is Overlay.ToolView -> ToolPage(o.id, o.expected) { overlay = null }
                        is Overlay.PostView -> {
                            val p = AppState.posts.firstOrNull { it.id == o.id }
                            if (p != null) PostDetailScreen(p) { overlay = null } else LaunchedEffect(o) { overlay = null }
                        }
                        is Overlay.AppView -> {
                            val a = AppState.apps.firstOrNull { it.id == o.id }
                            if (a != null) {
                                AppDetailScreen(
                                    app = a,
                                    onBack = { overlay = null },
                                    onVerify = { hash -> overlay = Overlay.ToolView("hash", hash) }
                                )
                            } else {
                                LaunchedEffect(o) { overlay = null }
                            }
                        }
                    }
                }
            }
        }

        UpdateDialog()
    }
}

@Composable
fun UpdateDialog() {
    val ctx = LocalContext.current
    val info = AppState.update
    if (info == null) return
    if (AppState.updateDismissed && !info.force) return
    Dialog(
        onDismissRequest = { if (!info.force) AppState.dismissUpdate() },
        properties = DialogProperties(
            dismissOnBackPress = !info.force,
            dismissOnClickOutside = !info.force
        )
    ) {
        val shape = RoundedCornerShape(26.dp)
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MColors.Surface)
                .border(1.2.dp, MColors.Lime.copy(alpha = 0.6f), shape)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MText("🚀", size = 40.sp)
            Spacer(Modifier.height(8.dp))
            MText("Update available", size = 20.sp, weight = FontWeight.Bold)
            MText(info.versionName, size = 13.sp, color = MColors.Lime, weight = FontWeight.SemiBold)
            if (info.notes.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                MText(
                    info.notes,
                    size = 12.sp,
                    color = MColors.Muted,
                    lineHeight = 18.sp,
                    align = TextAlign.Center,
                    maxLines = 8
                )
            }
            if (info.force) {
                Spacer(Modifier.height(10.dp))
                MText("This update is required to keep using MODMASE.", size = 11.sp, color = MColors.Warn, align = TextAlign.Center)
            }
            Spacer(Modifier.height(18.dp))
            GlowButton("Update now", "⬇️", true, { openUrl(ctx, info.url) }, Modifier.fillMaxWidth())
            if (!info.force) {
                Spacer(Modifier.height(10.dp))
                GlowButton("Later", "", false, { AppState.dismissUpdate() }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun FloatingNavBar(selected: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(32.dp)
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clip(shape)
            .background(Color(0xF2101612))
            .border(1.dp, MColors.Green.copy(alpha = 0.30f), shape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Tab.values().forEach { t ->
            NavItem(tab = t, selected = t == selected, onClick = { onSelect(t) })
        }
    }
}

@Composable
private fun NavItem(tab: Tab, selected: Boolean, onClick: () -> Unit) {
    val haptic = rememberHaptic()
    val bg by animateColorAsState(
        targetValue = if (selected) MColors.Green.copy(alpha = 0.20f) else Color.Transparent,
        label = "navBg"
    )
    val tint by animateColorAsState(
        targetValue = if (selected) MColors.Lime else MColors.Muted,
        label = "navTint"
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(26.dp))
            .background(bg)
            .clickable {
                haptic()
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        AnimatedVisibility(visible = selected) {
            MText(
                text = tab.label,
                size = 12.sp,
                weight = FontWeight.SemiBold,
                color = MColors.Lime,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}
