package com.tanzirdev.modmase

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Prefs {
    private var sp: SharedPreferences? = null

    var onboarded by mutableStateOf(false)
        private set
    var haptics by mutableStateOf(true)
        private set
    var materialYou by mutableStateOf(false)
        private set
    var pushEnabled by mutableStateOf(true)
        private set
    val topics = mutableStateMapOf<String, Boolean>()

    fun init(ctx: Context) {
        if (sp != null) return
        val p = ctx.applicationContext.getSharedPreferences("modmase", Context.MODE_PRIVATE)
        sp = p
        onboarded = p.getBoolean("onboarded", false)
        haptics = p.getBoolean("haptics", true)
        materialYou = p.getBoolean("material_you", false)
        pushEnabled = p.getBoolean("push", true)
        Push.TOPICS.forEach { topics[it] = p.getBoolean("topic_$it", true) }
    }

    private fun put(key: String, v: Boolean) {
        sp?.edit()?.putBoolean(key, v)?.apply()
    }

    @JvmName("applyOnboarded")
    fun setOnboarded(v: Boolean) { onboarded = v; put("onboarded", v) }
    @JvmName("applyHaptics")
    fun setHaptics(v: Boolean) { haptics = v; put("haptics", v) }
    @JvmName("applyMaterialYou")
    fun setMaterialYou(v: Boolean) { materialYou = v; put("material_you", v) }
    fun setPush(v: Boolean) { pushEnabled = v; put("push", v) }
    fun setTopic(t: String, v: Boolean) { topics[t] = v; put("topic_$t", v) }

    fun getLong(key: String): Long = sp?.getLong(key, 0L) ?: 0L
    fun putLong(key: String, v: Long) { sp?.edit()?.putLong(key, v)?.apply() }
}
