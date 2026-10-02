package com.tanzirdev.modmase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanzirdev.modmase.ui.AboutScreen
import com.tanzirdev.modmase.ui.AuroraBackground
import com.tanzirdev.modmase.ui.HomeScreen
import com.tanzirdev.modmase.ui.MColors
import com.tanzirdev.modmase.ui.MText
import com.tanzirdev.modmase.ui.ModmaseTheme
import com.tanzirdev.modmase.ui.SplashScreen
import com.tanzirdev.modmase.ui.ToolsScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModmaseTheme {
                ModmaseApp()
            }
        }
    }
}

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Tools("Tools", Icons.Filled.Build),
    About("About", Icons.Filled.Info)
}

@Composable
fun ModmaseApp() {
    val ctx = LocalContext.current
    val banner = remember { loadAssetBitmap(ctx, "images/modmase_banner.jpg") }
    val icon = remember { loadAssetBitmap(ctx, "images/modmase_icon.png") }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1800)
        showSplash = false
    }

    Box(Modifier.fillMaxSize().background(MColors.Bg)) {
        AuroraBackground()
        Crossfade(targetState = showSplash, animationSpec = tween(500), label = "splash") { splash ->
            if (splash) SplashScreen(icon) else MainShell(banner, icon)
        }
    }
}

@Composable
fun MainShell(banner: ImageBitmap, icon: ImageBitmap) {
    var tab by remember { mutableStateOf(Tab.Home) }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = tab,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "tabs"
        ) { current ->
            when (current) {
                Tab.Home -> HomeScreen(banner, icon)
                Tab.Tools -> ToolsScreen()
                Tab.About -> AboutScreen(icon)
            }
        }
        FloatingNavBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun FloatingNavBar(selected: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(32.dp)
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .clip(shape)
            .background(Color(0xF2101612))
            .border(1.dp, MColors.Green.copy(alpha = 0.30f), shape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Tab.values().forEach { t ->
            NavItem(tab = t, selected = t == selected, onClick = { onSelect(t) })
        }
    }
}

@Composable
private fun NavItem(tab: Tab, selected: Boolean, onClick: () -> Unit) {
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
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp),
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
                size = 13.sp,
                weight = FontWeight.SemiBold,
                color = MColors.Lime,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
