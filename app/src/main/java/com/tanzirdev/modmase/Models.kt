package com.tanzirdev.modmase

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

data class Post(
    val id: String,
    val title: String,
    val body: String,
    val category: String,
    val link: String,
    val thumb: String,
    val createdAt: Long,
    val pinned: Boolean
)

data class AppItem(
    val id: String,
    val name: String,
    val pkg: String,
    val category: String,
    val version: String,
    val size: String,
    val minAndroid: String,
    val modder: String,
    val shortDesc: String,
    val longDesc: String,
    val features: List<String>,
    val whatsNew: String,
    val tags: String,
    val icon: String,
    val downloadUrl: String,
    val sha256: String,
    val telegram: String,
    val featured: Boolean,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class UpdateConfig(
    val latestVersionName: String,
    val latestVersionCode: Long,
    val minVersionCode: Long,
    val apkUrl: String,
    val notes: String,
    val force: Boolean
)

data class UpdateInfo(val versionName: String, val url: String, val notes: String, val force: Boolean)

private fun objectOrNull(text: String?): JSONObject? {
    if (text.isNullOrBlank() || text == "null") return null
    return try {
        JSONObject(text)
    } catch (e: Exception) {
        null
    }
}

fun parsePosts(text: String?): List<Post> {
    val o = objectOrNull(text) ?: return emptyList()
    val out = ArrayList<Post>()
    val keys = o.keys()
    while (keys.hasNext()) {
        val k = keys.next()
        val p = o.optJSONObject(k) ?: continue
        out.add(
            Post(
                id = k,
                title = p.optString("title", ""),
                body = p.optString("body", ""),
                category = p.optString("category", "news"),
                link = p.optString("link", ""),
                thumb = p.optString("thumb", ""),
                createdAt = p.optLong("createdAt", 0L),
                pinned = p.optBoolean("pinned", false)
            )
        )
    }
    return out.sortedWith(compareByDescending<Post> { it.pinned }.thenByDescending { it.createdAt })
}

fun parseApps(text: String?): List<AppItem> {
    val o = objectOrNull(text) ?: return emptyList()
    val out = ArrayList<AppItem>()
    val keys = o.keys()
    while (keys.hasNext()) {
        val k = keys.next()
        val a = o.optJSONObject(k) ?: continue
        out.add(
            AppItem(
                id = k,
                name = a.optString("name", ""),
                pkg = a.optString("pkg", ""),
                category = a.optString("category", "apps"),
                version = a.optString("version", ""),
                size = a.optString("size", ""),
                minAndroid = a.optString("minAndroid", ""),
                modder = a.optString("modder", ""),
                shortDesc = a.optString("shortDesc", ""),
                longDesc = a.optString("longDesc", ""),
                features = a.optString("features", "").split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                whatsNew = a.optString("whatsNew", ""),
                tags = a.optString("tags", ""),
                icon = a.optString("icon", ""),
                downloadUrl = a.optString("downloadUrl", ""),
                sha256 = a.optString("sha256", ""),
                telegram = a.optString("telegram", ""),
                featured = a.optBoolean("featured", false),
                status = a.optString("status", "published"),
                createdAt = a.optLong("createdAt", 0L),
                updatedAt = a.optLong("updatedAt", 0L)
            )
        )
    }
    return out.sortedByDescending { it.updatedAt }
}

fun parseConfig(text: String?): UpdateConfig? {
    val o = objectOrNull(text) ?: return null
    return UpdateConfig(
        latestVersionName = o.optString("latestVersionName", ""),
        latestVersionCode = o.optLong("latestVersionCode", 0L),
        minVersionCode = o.optLong("minVersionCode", 0L),
        apkUrl = o.optString("apkUrl", ""),
        notes = o.optString("notes", ""),
        force = o.optBoolean("force", false)
    )
}

fun parseDownloads(text: String?): Map<String, Long> {
    val o = objectOrNull(text) ?: return emptyMap()
    val out = HashMap<String, Long>()
    val keys = o.keys()
    while (keys.hasNext()) {
        val k = keys.next()
        out[k] = o.optLong(k, 0L)
    }
    return out
}

/** Reads a JSON string value such as "data:image/jpeg;base64,..." */
fun parseJsonString(text: String?): String {
    if (text.isNullOrBlank() || text == "null") return ""
    return try {
        (JSONTokener(text).nextValue() as? String) ?: ""
    } catch (e: Exception) {
        ""
    }
}

/** Reads a JSON array (or object with numeric keys) of strings. */
fun parseStringList(text: String?): List<String> {
    if (text.isNullOrBlank() || text == "null") return emptyList()
    val v = try {
        JSONTokener(text).nextValue()
    } catch (e: Exception) {
        return emptyList()
    }
    val out = ArrayList<String>()
    if (v is JSONArray) {
        for (i in 0 until v.length()) {
            val s = v.optString(i, "")
            if (s.isNotBlank() && s != "null") out.add(s)
        }
    } else if (v is JSONObject) {
        val keys = v.keys().asSequence().toList().sorted()
        for (k in keys) {
            val s = v.optString(k, "")
            if (s.isNotBlank() && s != "null") out.add(s)
        }
    }
    return out
}

/** Compares versions like "v2.0.1" and "2.0". Returns >0 when a is newer. */
fun compareVersions(a: String, b: String): Int {
    fun parts(s: String): List<Long> =
        s.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }.map { it.toLongOrNull() ?: 0L }
    val pa = parts(a)
    val pb = parts(b)
    val n = maxOf(pa.size, pb.size)
    for (i in 0 until n) {
        val x = if (i < pa.size) pa[i] else 0L
        val y = if (i < pb.size) pb[i] else 0L
        if (x != y) return if (x > y) 1 else -1
    }
    return 0
}

fun timeAgo(ts: Long): String {
    if (ts <= 0L) return ""
    val diff = System.currentTimeMillis() - ts
    val min = diff / 60000
    return when {
        min < 1 -> "just now"
        min < 60 -> "${min}m ago"
        min < 60 * 24 -> "${min / 60}h ago"
        min < 60 * 24 * 30 -> "${min / (60 * 24)}d ago"
        else -> "${min / (60 * 24 * 30)}mo ago"
    }
}
