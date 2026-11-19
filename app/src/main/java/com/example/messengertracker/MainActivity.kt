package com.example.messengertracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var isTracking = false
    private lateinit var logView: TextView

    private val checkRunnable = object : Runnable {
        override fun run() {
            if (!isTracking) return
            val launchIntent = packageManager.getLaunchIntentForPackage("com.facebook.orca")
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
            handler.postDelayed(this, 300000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("TrackerPrefs", MODE_PRIVATE)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }

        val input1 = EditText(this).apply {
            hint = "Nome Profilo 1 su Messenger"
            setText(prefs.getString("profile1", ""))
        }

        val input2 = EditText(this).apply {
            hint = "Nome Profilo 2 su Messenger"
            setText(prefs.getString("profile2", ""))
        }

        val btnSave = Button(this).apply {
            text = "1. Salva Nomi"
            setOnClickListener {
                prefs.edit()
                    .putString("profile1", input1.text.toString())
                    .putString("profile2", input2.text.toString())
                    .apply()
                Toast.makeText(this@MainActivity, "Nomi salvati", Toast.LENGTH_SHORT).show()
            }
        }

        val btnAccessibility = Button(this).apply {
            text = "2. Attiva Permesso Accessibilità"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val btnToggle = Button(this).apply {
            text = "3. Avvia Monitoraggio (Ogni 5 min)"
            setOnClickListener {
                isTracking = !isTracking
                if (isTracking) {
                    text = "Ferma Monitoraggio"
                    handler.post(checkRunnable)
                } else {
                    text = "Avvia Monitoraggio (Ogni 5 min)"
                    handler.removeCallbacks(checkRunnable)
                }
            }
        }

        val btnRefreshLogs = Button(this).apply {
            text = "Aggiorna Log e Calcola Sovrapposizioni"
            setOnClickListener { updateLogsDisplay() }
        }

        logView = TextView(this).apply {
            setPadding(0, 32, 0, 0)
            textSize = 13f
        }

        val scrollView = ScrollView(this).apply {
            addView(logView)
        }

        layout.addView(input1)
        layout.addView(input2)
        layout.addView(btnSave)
        layout.addView(btnAccessibility)
        layout.addView(btnToggle)
        layout.addView(btnRefreshLogs)
        layout.addView(scrollView)

        setContentView(layout)
        updateLogsDisplay()
    }

    private fun updateLogsDisplay() {
        val file = File(filesDir, "presence_logs.txt")
        if (!file.exists()) {
            logView.text = "Nessun log registrato finora."
            return
        }
        val lines = file.readLines()
        val overlapCount = lines.count { it.contains("[ENTRAMBI ONLINE]") }
        val estimatedMinutes = overlapCount * 5

        logView.text = "Rilevazioni in comune: $overlapCount (circa $estimatedMinutes min)\n\n" +
                lines.takeLast(50).reversed().joinToString("\n")
    }
}
