package com.tanzirdev.modmase.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanzirdev.modmase.Api
import com.tanzirdev.modmase.AppState
import com.tanzirdev.modmase.Links
import com.tanzirdev.modmase.Post
import com.tanzirdev.modmase.openLinkOrChannel
import com.tanzirdev.modmase.openTelegram
import com.tanzirdev.modmase.openUrl
import com.tanzirdev.modmase.parseJsonString
import com.tanzirdev.modmase.shareText
import com.tanzirdev.modmase.timeAgo
import kotlinx.coroutines.launch

fun categoryLabel(c: String): String = when (c) {
    "mods" -> "🔧 MODS"
    "apps" -> "📱 APPS"
    "games" -> "🎮 GAMES"
    "tools" -> "🧰 TOOLS"
    else -> "📰 NEWS"
}

private class Cat(val key: String, val emoji: String, val title: String, val subtitle: String)

@Composable
fun HomeScreen(
    banner: ImageBitmap,
    icon: ImageBitmap,
    onOpenCategory: (String) -> Unit,
    onOpenPost: (Post) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val categories = remember {
        listOf(
            Cat("mods", "🔧", "Android MODs", "Tweaks & patches"),
            Cat("apps", "📱", "Apps & Tools", "Useful picks"),
            Cat("games", "🎮", "Games", "Game mods"),
            Cat("tools", "🧰", "Tools", "Utilities")
        )
    }
    val posts = AppState.posts

    ScreenColumn {
        Reveal(0) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RoundIcon(icon)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    MText("MODMASE", size = 22.sp, weight = FontWeight.Bold, letterSpacing = 1.sp)
                    MText("Your Modding Destination", size = 12.sp, color = MColors.Muted)
                }
                LiveBadge()
            }
        }
        Spacer(Modifier.height(22.dp))
        Reveal(1) { BannerCard(banner) }
        Spacer(Modifier.height(20.dp))
        Reveal(2) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                MText("Mod Better, Live Smarter", script = true, size = 24.sp, color = MColors.Lime)
                Spacer(Modifier.height(4.dp))
                MText("Mods · Apps · Tools · More", size = 12.sp, color = MColors.Muted, letterSpacing = 2.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Reveal(3) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlowButton("Join Telegram", "✈️", true, { openTelegram(ctx) }, Modifier.weight(1f))
                GlowButton("Open Website", "🌐", false, { openUrl(ctx, Links.WEBSITE) }, Modifier.weight(1f))
            }
        }

        // ---- Live updates feed ----
        Reveal(4) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Latest updates", Modifier.weight(1f))
                Box(
                    Modifier
                        .padding(top = 14.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MColors.Surface)
                        .border(1.dp, MColors.Green.copy(alpha = 0.3f), CircleShape)
                        .clickable { scope.launch { AppState.refresh(ctx) } },
                    contentAlignment = Alignment.Center
                ) {
                    MText(if (AppState.loading) "⏳" else "🔄", size = 15.sp)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when {
                posts.isEmpty() && AppState.loading -> {
                    PostSkeleton()
                    PostSkeleton()
                }
                posts.isEmpty() && AppState.failed -> ErrorState { scope.launch { AppState.refresh(ctx) } }
                posts.isEmpty() -> EmptyState("No updates yet", "New mods and news will appear here.")
                else -> posts.take(20).forEachIndexed { i, p ->
                    Reveal(if (i < 5) i else 5) {
                        PostCard(p) { onOpenPost(p) }
                    }
                }
            }
        }

        // ---- Categories ----
        Reveal(6) { SectionTitle("Browse by category") }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { cat ->
                        GlassCard(Modifier.weight(1f), onClick = { onOpenCategory(cat.key) }) {
                            MText(cat.emoji, size = 26.sp)
                            Spacer(Modifier.height(10.dp))
                            MText(cat.title, size = 14.sp, weight = FontWeight.SemiBold)
                            MText(cat.subtitle, size = 11.sp, color = MColors.Muted)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(26.dp))
        MText(
            "💚 Stay tuned. Discover. Mod. Enjoy.",
            modifier = Modifier.fillMaxWidth(),
            size = 13.sp,
            color = MColors.Muted,
            align = TextAlign.Center
        )
    }
}

@Composable
fun PostCard(post: Post, onOpen: () -> Unit) {
    val ctx = LocalContext.current
    val haptic = rememberHaptic()
    val img = rememberDataImage(post.thumb)
    val shape = RoundedCornerShape(22.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(MColors.SurfaceHigh, MColors.Surface)))
            .border(1.dp, MColors.Green.copy(alpha = 0.18f), shape)
            .clickable {
                haptic()
                onOpen()
            }
    ) {
        if (img != null) {
            Image(
                bitmap = img,
                contentDescription = post.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
        }
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Tag(categoryLabel(post.category))
                if (post.pinned) {
                    Spacer(Modifier.width(8.dp))
                    Tag("📌 PINNED", MColors.Warn)
                }
                Spacer(Modifier.weight(1f))
                MText(timeAgo(post.createdAt), size = 11.sp, color = MColors.Muted)
            }
            Spacer(Modifier.height(10.dp))
            MText(post.title, size = 16.sp, weight = FontWeight.Bold, maxLines = 2)
            if (post.body.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                MText(post.body, size = 12.sp, color = MColors.Muted, lineHeight = 18.sp, maxLines = 3)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlowButton(
                    "Open in Telegram", "✈️", true,
                    { openLinkOrChannel(ctx, post.link) },
                    Modifier.weight(1f)
                )
                GlowButton(
                    "Share", "", false,
                    { shareText(ctx, post.title + "\n\n" + (if (post.link.isNotBlank()) post.link else Links.TELEGRAM)) },
                    Modifier.width(92.dp)
                )
            }
        }
    }
}

@Composable
fun PostDetailScreen(post: Post, onBack: () -> Unit) {
    val ctx = LocalContext.current
    var full by remember(post.id) { mutableStateOf("") }
    LaunchedEffect(post.id) {
        full = parseJsonString(Api.get("postMedia/${post.id}"))
    }
    val img = rememberDataImage(if (full.isNotEmpty()) full else post.thumb)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Update", "📰", onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            if (img != null) {
                Image(
                    bitmap = img,
                    contentDescription = post.title,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.dp, MColors.Green.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                )
                Spacer(Modifier.height(18.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Tag(categoryLabel(post.category))
                Spacer(Modifier.width(10.dp))
                MText(timeAgo(post.createdAt), size = 11.sp, color = MColors.Muted)
            }
            Spacer(Modifier.height(12.dp))
            MText(post.title, size = 22.sp, weight = FontWeight.Bold, lineHeight = 30.sp)
            if (post.body.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                MText(post.body, size = 14.sp, color = MColors.TextPrimary, lineHeight = 22.sp)
            }
            Spacer(Modifier.height(24.dp))
            GlowButton(
                "Open in Telegram", "✈️", true,
                { openLinkOrChannel(ctx, post.link) },
                Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            GlowButton(
                "Share", "📤", false,
                { shareText(ctx, post.title + "\n\n" + (if (post.link.isNotBlank()) post.link else Links.TELEGRAM)) },
                Modifier.fillMaxWidth()
            )
        }
    }
}
