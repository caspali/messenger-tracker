package com.example.messengertracker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessengerAccessibilityService : AccessibilityService() {

    private var lastCheckTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCheckTime < 10000) return
        lastCheckTime = currentTime

        val rootNode = rootInActiveWindow ?: return
        val prefs = getSharedPreferences("TrackerPrefs", MODE_PRIVATE)
        val profile1 = prefs.getString("profile1", "")?.trim() ?: ""
        val profile2 = prefs.getString("profile2", "")?.trim() ?: ""

        if (profile1.isEmpty() || profile2.isEmpty()) return

        val screenTexts = mutableListOf<String>()
        extractTextNodes(rootNode, screenTexts)

        val p1Online = isProfileOnline(screenTexts, profile1)
        val p2Online = isProfileOnline(screenTexts, profile2)

        if (p1Online || p2Online) {
            saveLog(profile1, p1Online, profile2, p2Online)
        }
    }

    private fun extractTextNodes(node: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (node == null) return
        node.text?.toString()?.let { if (it.isNotBlank()) list.add(it) }
        node.contentDescription?.toString()?.let { if (it.isNotBlank()) list.add(it) }
        for (i in 0 until node.childCount) {
            extractTextNodes(node.getChild(i), list)
        }
    }

    private fun isProfileOnline(texts: List<String>, targetName: String): Boolean {
        for (i in texts.indices) {
            if (texts[i].contains(targetName, ignoreCase = true)) {
                val window = texts.subList(maxOf(0, i - 2), minOf(texts.size, i + 4)).joinToString(" ")
                if (window.contains("Attivo ora", ignoreCase = true) ||
                    window.contains("Active now", ignoreCase = true) ||
                    window.contains("Online", ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    private fun saveLog(p1: String, p1Online: Boolean, p2: String, p2Online: Boolean) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val bothOnline = if (p1Online && p2Online) " [ENTRAMBI ONLINE]" else ""
        val logLine = "$timestamp | $p1: ${if (p1Online) "ON" else "OFF"} | $p2: ${if (p2Online) "ON" else "OFF"}$bothOnline\n"

        val file = File(filesDir, "presence_logs.txt")
        file.appendText(logLine)
    }

    override fun onInterrupt() {}
}