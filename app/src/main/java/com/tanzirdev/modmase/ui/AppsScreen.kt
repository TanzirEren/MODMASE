package com.tanzirdev.modmase.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanzirdev.modmase.Api
import com.tanzirdev.modmase.AppItem
import com.tanzirdev.modmase.AppState
import com.tanzirdev.modmase.Links
import com.tanzirdev.modmase.copyText
import com.tanzirdev.modmase.openLinkOrChannel
import com.tanzirdev.modmase.openUrl
import com.tanzirdev.modmase.parseStringList
import com.tanzirdev.modmase.shareText
import com.tanzirdev.modmase.timeAgo
import kotlinx.coroutines.launch

private val AppCategories = listOf(
    "all" to "All",
    "mods" to "🔧 Mods",
    "apps" to "📱 Apps",
    "games" to "🎮 Games",
    "tools" to "🧰 Tools"
)

@Composable
fun AppIcon(app: AppItem, size: Int) {
    val img = rememberDataImage(app.icon)
    val shape = RoundedCornerShape((size / 4).dp)
    if (img != null) {
        Image(
            bitmap = img,
            contentDescription = app.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size.dp)
                .clip(shape)
                .border(1.dp, MColors.Green.copy(alpha = 0.3f), shape)
        )
    } else {
        Box(
            Modifier
                .size(size.dp)
                .clip(shape)
                .background(Brush.linearGradient(listOf(MColors.DeepGreen, MColors.Green)))
                .border(1.dp, MColors.Green.copy(alpha = 0.3f), shape),
            contentAlignment = Alignment.Center
        ) {
            MText(
                app.name.take(1).uppercase(),
                size = (size / 2.2f).sp,
                weight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.Black
            )
        }
    }
}

@Composable
fun AppsScreen(initialCategory: String, onOpenApp: (AppItem) -> Unit) {
    var query by remember { mutableStateOf("") }
    var cat by remember(initialCategory) { mutableStateOf(initialCategory) }
    val all = AppState.apps.filter { it.status != "draft" }
    val filtered = all.filter { a ->
        (cat == "all" || a.category == cat) &&
            (query.isBlank() || listOf(a.name, a.pkg, a.tags, a.shortDesc, a.modder).any { it.contains(query, ignoreCase = true) })
    }
    val featured = all.filter { it.featured }

    ScreenColumn {
        Reveal(0) {
            Column {
                MText("Apps library", size = 26.sp, weight = FontWeight.Bold)
                MText("MODs, apps, games & tools", size = 12.sp, color = MColors.Muted)
            }
        }
        Spacer(Modifier.height(16.dp))
        Reveal(1) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MColors.Surface)
                    .border(1.dp, MColors.Green.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MText("🔍", size = 15.sp)
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) MText("Search apps, mods, games...", size = 14.sp, color = MColors.Muted)
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MColors.TextPrimary,
                            fontSize = 14.sp,
                            fontFamily = LocalFonts.current.body
                        ),
                        cursorBrush = SolidColor(MColors.Lime),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (query.isNotEmpty()) {
                    Box(Modifier.clickable { query = "" }.padding(4.dp)) {
                        MText("✕", size = 14.sp, color = MColors.Muted)
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppCategories.forEach { (key, label) ->
                val count = if (key == "all") all.size else all.count { it.category == key }
                Chip("$label · $count", cat == key) { cat = key }
            }
        }

        if (featured.isNotEmpty() && query.isBlank() && cat == "all") {
            SectionTitle("⭐ Featured")
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                featured.forEach { a ->
                    Column(Modifier.width(250.dp)) {
                        GlassCard(onClick = { onOpenApp(a) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppIcon(a, 52)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    MText(a.name, size = 14.sp, weight = FontWeight.Bold, maxLines = 1)
                                    MText(a.version, size = 11.sp, color = MColors.Muted, maxLines = 1)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            MText(a.shortDesc, size = 11.sp, color = MColors.Muted, lineHeight = 16.sp, maxLines = 2)
                        }
                    }
                }
            }
        }

        SectionTitle(if (cat == "all") "All apps" else "Results")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when {
                filtered.isEmpty() && AppState.loading -> {
                    AppRowSkeleton()
                    AppRowSkeleton()
                    AppRowSkeleton()
                }
                filtered.isEmpty() -> EmptyState(
                    "Nothing here yet",
                    if (query.isBlank()) "Apps added from the admin panel will show up here." else "Try a different search or category."
                )
                else -> filtered.forEachIndexed { i, a ->
                    Reveal(if (i < 6) i else 6) { AppRow(a) { onOpenApp(a) } }
                }
            }
        }
    }
}

@Composable
fun AppRow(app: AppItem, onClick: () -> Unit) {
    val downloads = AppState.downloads[app.id] ?: 0L
    GlassCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(app, 56)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                MText(app.name, size = 15.sp, weight = FontWeight.Bold, maxLines = 1)
                val meta = listOf(app.version, app.size).filter { it.isNotBlank() }.joinToString(" · ")
                if (meta.isNotEmpty()) MText(meta, size = 11.sp, color = MColors.Muted, maxLines = 1)
                if (app.shortDesc.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    MText(app.shortDesc, size = 11.sp, color = MColors.Muted, lineHeight = 16.sp, maxLines = 2)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Tag(categoryLabel(app.category))
                if (downloads > 0) {
                    Spacer(Modifier.height(6.dp))
                    MText("⬇ $downloads", size = 10.sp, color = MColors.Muted)
                }
            }
        }
    }
}

@Composable
fun AppDetailScreen(app: AppItem, onBack: () -> Unit, onVerify: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var shots by remember(app.id) { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(app.id) {
        shots = parseStringList(Api.get("appMedia/${app.id}"))
    }
    val downloads = AppState.downloads[app.id] ?: 0L

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(app.name, "", onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app, 92)
                Spacer(Modifier.width(16.dp))
                Column {
                    MText(app.name, size = 22.sp, weight = FontWeight.Bold, maxLines = 2)
                    val meta = listOf(app.version, app.size).filter { it.isNotBlank() }.joinToString(" · ")
                    if (meta.isNotEmpty()) MText(meta, size = 12.sp, color = MColors.Muted)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Tag(categoryLabel(app.category))
                        if (app.category == "mods") Tag("MOD", MColors.Warn)
                        if (downloads > 0) Tag("⬇ $downloads", MColors.Muted)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlowButton(
                    "Download", "⬇️", true,
                    {
                        if (app.downloadUrl.isBlank()) {
                            Toaster.show("Download link isn't available yet. Check Telegram.", ToastType.Info)
                        } else {
                            openUrl(ctx, app.downloadUrl)
                            scope.launch { AppState.countDownload(app.id) }
                        }
                    },
                    Modifier.weight(1f)
                )
                GlowButton("Telegram", "✈️", false, { openLinkOrChannel(ctx, app.telegram) }, Modifier.weight(1f))
            }

            if (shots.isNotEmpty()) {
                SectionTitle("Screenshots")
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    shots.forEach { s ->
                        val img = rememberDataImage(s)
                        if (img != null) {
                            Image(
                                bitmap = img,
                                contentDescription = "Screenshot",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .height(260.dp)
                                    .width(146.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, MColors.Green.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            )
                        }
                    }
                }
            }

            val about = if (app.longDesc.isNotBlank()) app.longDesc else app.shortDesc
            if (about.isNotBlank()) {
                SectionTitle("About")
                GlassCard { MText(about, size = 13.sp, lineHeight = 21.sp) }
            }
            if (app.features.isNotEmpty()) {
                SectionTitle("MOD features")
                GlassCard {
                    app.features.forEach { f ->
                        Row(Modifier.padding(vertical = 4.dp)) {
                            MText("✓", size = 13.sp, color = MColors.Lime, weight = FontWeight.Bold)
                            Spacer(Modifier.width(10.dp))
                            MText(f, size = 13.sp, lineHeight = 19.sp)
                        }
                    }
                }
            }
            if (app.whatsNew.isNotBlank()) {
                SectionTitle("What's new")
                GlassCard { MText(app.whatsNew, size = 13.sp, lineHeight = 21.sp) }
            }

            SectionTitle("Information")
            GlassCard {
                InfoLine("Package", app.pkg)
                InfoLine("Version", app.version)
                InfoLine("Size", app.size)
                InfoLine("Requires", app.minAndroid)
                InfoLine("Modder", app.modder)
                InfoLine("Updated", timeAgo(app.updatedAt))
            }

            if (app.sha256.isNotBlank()) {
                SectionTitle("File integrity")
                GlassCard {
                    MText("SHA-256", size = 11.sp, color = MColors.Muted)
                    Spacer(Modifier.height(4.dp))
                    MText(app.sha256, size = 11.sp, lineHeight = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlowButton("Copy", "📋", false, { copyText(ctx, "SHA-256", app.sha256) }, Modifier.weight(1f))
                        GlowButton("Verify file", "🔐", true, { onVerify(app.sha256) }, Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            GlowButton(
                "Share this app", "📤", false,
                { shareText(ctx, app.name + " " + app.version + "\n\n" + Links.TELEGRAM) },
                Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MText(label, size = 12.sp, color = MColors.Muted)
        Spacer(Modifier.width(12.dp))
        MText(value, size = 12.sp, weight = FontWeight.Medium, align = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.weight(1f))
    }
}
