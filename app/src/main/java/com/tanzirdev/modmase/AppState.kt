package com.tanzirdev.modmase

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.pm.PackageInfoCompat
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject

object AppState {
    var posts by mutableStateOf<List<Post>>(emptyList())
        private set
    var apps by mutableStateOf<List<AppItem>>(emptyList())
        private set
    var downloads by mutableStateOf<Map<String, Long>>(emptyMap())
        private set
    var config by mutableStateOf<UpdateConfig?>(null)
        private set
    var update by mutableStateOf<UpdateInfo?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var failed by mutableStateOf(false)
        private set
    var updateDismissed by mutableStateOf(false)
        private set

    // deep-link / shortcut requests handled by the UI
    var pendingType by mutableStateOf("")
    var pendingId by mutableStateOf("")
    var pendingTab by mutableStateOf("")

    fun setPending(type: String, id: String) {
        pendingType = type
        pendingId = id
    }

    fun clearPending() {
        pendingType = ""
        pendingId = ""
    }

    fun dismissUpdate() {
        updateDismissed = true
    }

    private fun readFile(ctx: Context, name: String): String? =
        try {
            val f = File(ctx.cacheDir, name)
            if (f.exists()) f.readText() else null
        } catch (e: Exception) {
            null
        }

    private fun writeFile(ctx: Context, name: String, text: String) {
        try {
            File(ctx.cacheDir, name).writeText(text)
        } catch (e: Exception) {
            // ignore cache failures
        }
    }

    suspend fun loadCache(ctx: Context) {
        val app = ctx.applicationContext
        val result = withContext(Dispatchers.Default) {
            Triple(
                parsePosts(readFile(app, "posts.json")),
                parseApps(readFile(app, "apps.json")),
                parseDownloads(readFile(app, "downloads.json"))
            )
        }
        if (posts.isEmpty()) posts = result.first
        if (apps.isEmpty()) apps = result.second
        if (downloads.isEmpty()) downloads = result.third
    }

    suspend fun refresh(ctx: Context) {
        val app = ctx.applicationContext
        loading = true
        failed = false
        coroutineScope {
            val dp = async { Api.get("posts", "?orderBy=%22createdAt%22&limitToLast=40") ?: Api.get("posts") }
            val da = async { Api.get("apps") }
            val dc = async { Api.get("config") }
            val dd = async { Api.get("stats/downloads") }
            val pt = dp.await()
            val at = da.await()
            val ct = dc.await()
            val dt = dd.await()
            if (pt == null && at == null && ct == null) failed = true
            if (pt != null) {
                posts = withContext(Dispatchers.Default) { parsePosts(pt) }
                writeFile(app, "posts.json", pt)
                saveLatest(app)
            }
            if (at != null) {
                apps = withContext(Dispatchers.Default) { parseApps(at) }
                writeFile(app, "apps.json", at)
            }
            if (dt != null) {
                downloads = parseDownloads(dt)
                writeFile(app, "downloads.json", dt)
            }
            if (ct != null) config = parseConfig(ct)
        }
        checkUpdate(app)
        loading = false
    }

    private fun saveLatest(ctx: Context) {
        val latest = posts.maxByOrNull { it.createdAt } ?: return
        val sp = ctx.getSharedPreferences("modmase_widget", Context.MODE_PRIVATE)
        sp.edit().putString("title", latest.title).putString("link", latest.link).apply()
        ModmaseWidget.refresh(ctx)
    }

    private suspend fun checkUpdate(ctx: Context) {
        try {
            val pi = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            val curCode = PackageInfoCompat.getLongVersionCode(pi)
            val curName = pi.versionName ?: "0"
            val c = config
            if (c != null && c.latestVersionCode > 0) {
                update = if (c.latestVersionCode > curCode) {
                    UpdateInfo(
                        c.latestVersionName.ifBlank { "new version" },
                        c.apkUrl.ifBlank { Links.TELEGRAM },
                        c.notes,
                        c.force || curCode < c.minVersionCode
                    )
                } else {
                    null
                }
                return
            }
            // Fallback: ask GitHub for the latest release
            val gh = withContext(Dispatchers.IO) { Api.getSync(Api.GITHUB_LATEST) } ?: return
            val o = JSONObject(gh)
            val tag = o.optString("tag_name", "")
            if (tag.isNotBlank() && compareVersions(tag, curName) > 0) {
                var url = o.optString("html_url", Links.TELEGRAM)
                val assets = o.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val a = assets.optJSONObject(i) ?: continue
                        if (a.optString("name", "").endsWith(".apk")) {
                            url = a.optString("browser_download_url", url)
                            break
                        }
                    }
                }
                update = UpdateInfo(tag, url, o.optString("body", ""), false)
            } else {
                update = null
            }
        } catch (e: Exception) {
            // ignore update check failures
        }
    }

    /** Manual check from Settings. Returns true if an update is available. */
    suspend fun manualCheck(ctx: Context): Boolean {
        updateDismissed = false
        refresh(ctx)
        return update != null
    }

    suspend fun countDownload(id: String) {
        val server = Api.get("stats/downloads/$id")?.trim()?.toLongOrNull() ?: 0L
        val ok = Api.send("PUT", "stats/downloads/$id", (server + 1).toString())
        if (ok != null) downloads = downloads + (id to (server + 1))
    }
}
