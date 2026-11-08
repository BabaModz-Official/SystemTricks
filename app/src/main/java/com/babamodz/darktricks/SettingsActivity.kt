package com.babamodz.darktricks

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val ACTION_PREFERENCES_CHANGED = "com.babamodz.darktricks.PREFERENCES"
        const val PREF_FILE_NAME = "com.babamodz.darktricks_shared"
        const val GITHUB_URL = "https://github.com/BabaModz-Official/SystemTricks/releases"

        @JvmStatic
        fun notifyPreferenceChanged(context: Context, key: String, value: Any) {
            val intent = Intent(ACTION_PREFERENCES_CHANGED).apply {
                putExtra("preference", key)
                when (value) {
                    is Boolean -> putExtra("value", value)
                    is String -> putExtra("value", value)
                    is Int -> putExtra("value", value.toString())
                    is Long -> putExtra("value", value.toString())
                }
            }

            // General broadcast
            context.sendBroadcast(intent)

            // Explicit broadcast to android (system_server)
            val systemIntent = Intent(intent).apply { setPackage("android") }
            context.sendBroadcast(systemIntent)

            // Explicit broadcast to SystemUI
            val sysuiIntent = Intent(intent).apply { setPackage("com.android.systemui") }
            context.sendBroadcast(sysuiIntent)

            // Notify ContentProvider
            try {
                context.contentResolver.notifyChange(PreferencesProvider.CONTENT_URI, null)
            } catch (t: Throwable) {
                // Ignore
            }
        }
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: TweakAdapter
    private var allTweaks: List<TweakDef> = emptyList()

    private var currentFilterCategory: TweakCategory = TweakCategory.ALL
    private var currentSearchQuery: String = ""

    private lateinit var cardStatus: MaterialCardView
    private lateinit var badgeStatus: TextView
    private lateinit var badgeVersion: TextView
    private lateinit var statusTitle: TextView
    private lateinit var statusSubtitle: TextView
    private lateinit var btnOpenLsposed: MaterialButton

    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: ImageView
    private lateinit var chipGroup: ChipGroup
    private lateinit var rvTweaks: RecyclerView
    private lateinit var layoutEmptySearch: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        allTweaks = TweakRepository.getAllTweaks(this)

        initViews()
        setupStatusCard()
        setupRecyclerView()
        setupSearchAndFilters()
        setupTopBarActions()

        refreshList()
    }

    private fun initViews() {
        cardStatus = findViewById(R.id.card_status)
        badgeStatus = findViewById(R.id.badge_status)
        badgeVersion = findViewById(R.id.badge_version)
        statusTitle = findViewById(R.id.status_title)
        statusSubtitle = findViewById(R.id.status_subtitle)
        btnOpenLsposed = findViewById(R.id.btn_open_lsposed)

        etSearch = findViewById(R.id.et_search)
        btnClearSearch = findViewById(R.id.btn_clear_search)
        chipGroup = findViewById(R.id.chip_group_categories)
        rvTweaks = findViewById(R.id.rv_tweaks)
        layoutEmptySearch = findViewById(R.id.layout_empty_search)
    }

    private fun setupStatusCard() {
        val isActive = ModuleStatus.isModuleActive()
        val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} ${Build.MODEL}"
        badgeVersion.text = "Android ${Build.VERSION.RELEASE} • API ${Build.VERSION.SDK_INT}"

        if (isActive) {
            cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_active_bg))
            cardStatus.strokeColor = ContextCompat.getColor(this, R.color.status_active_border)
            badgeStatus.setBackgroundResource(R.drawable.bg_status_pill)
            badgeStatus.text = "● ACTIVE (API 93)"
            badgeStatus.setTextColor(ContextCompat.getColor(this, R.color.status_active_text))

            statusTitle.text = "SystemTricks Activated"
            statusTitle.setTextColor(ContextCompat.getColor(this, R.color.status_active_text))

            statusSubtitle.text = "$deviceModel • Hooks active in system_server, SystemUI & IME.\nTweaks apply in real time."
            statusSubtitle.setTextColor(Color.parseColor("#2E7D32"))
            btnOpenLsposed.visibility = View.GONE
        } else {
            cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_inactive_bg))
            cardStatus.strokeColor = ContextCompat.getColor(this, R.color.status_inactive_border)
            badgeStatus.setBackgroundResource(R.drawable.bg_status_pill_inactive)
            badgeStatus.text = "● NOT ACTIVATED"
            badgeStatus.setTextColor(ContextCompat.getColor(this, R.color.status_inactive_text))

            statusTitle.text = "Module Not Activated"
            statusTitle.setTextColor(ContextCompat.getColor(this, R.color.status_inactive_text))

            statusSubtitle.text = "1. Open LSPosed Manager\n2. Enable SystemTricks & check 'System Framework' scope\n3. Reboot device (Required once for system_server)"
            statusSubtitle.setTextColor(Color.parseColor("#C62828"))
            btnOpenLsposed.visibility = View.VISIBLE
            btnOpenLsposed.setOnClickListener { openLsposed() }
        }
    }

    private fun setupRecyclerView() {
        adapter = TweakAdapter(this, prefs) { key, value ->
            notifyPreferenceChanged(this, key, value)
        }
        rvTweaks.layoutManager = LinearLayoutManager(this)
        rvTweaks.adapter = adapter
    }

    private fun setupSearchAndFilters() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                btnClearSearch.visibility = if (currentSearchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                refreshList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnClearSearch.setOnClickListener {
            etSearch.setText("")
        }

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilterCategory = when {
                checkedIds.contains(R.id.chip_hardware) -> TweakCategory.HARDWARE
                checkedIds.contains(R.id.chip_notifications) -> TweakCategory.NOTIFICATIONS
                checkedIds.contains(R.id.chip_statusbar) -> TweakCategory.STATUS_BAR
                checkedIds.contains(R.id.chip_apps) -> TweakCategory.APPS
                else -> TweakCategory.ALL
            }
            refreshList()
        }
    }

    private fun setupTopBarActions() {
        findViewById<View>(R.id.btn_github).setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
            } catch (t: Throwable) {
                Toast.makeText(this, "Could not open browser", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btn_reset).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Reset to Defaults?")
                .setMessage("All tweaks will be reset to their default disabled states.")
                .setPositiveButton("Reset") { _, _ ->
                    resetAllDefaults()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<View>(R.id.btn_info).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun refreshList() {
        val query = currentSearchQuery.lowercase(Locale.ROOT)

        val filteredTweaks = allTweaks.filter { tweak ->
            val matchesCategory = (currentFilterCategory == TweakCategory.ALL || tweak.category == currentFilterCategory)
            val matchesSearch = query.isEmpty() ||
                    tweak.title.lowercase(Locale.ROOT).contains(query) ||
                    tweak.summary.lowercase(Locale.ROOT).contains(query)
            matchesCategory && matchesSearch
        }

        if (filteredTweaks.isEmpty()) {
            layoutEmptySearch.visibility = View.VISIBLE
            rvTweaks.visibility = View.GONE
            adapter.submitItems(emptyList())
            return
        }

        layoutEmptySearch.visibility = View.GONE
        rvTweaks.visibility = View.VISIBLE

        val listItems = mutableListOf<ListItem>()

        if (currentFilterCategory == TweakCategory.ALL && query.isEmpty()) {
            // Group with stylish Category Headers
            val grouped = filteredTweaks.groupBy { it.category }
            for ((cat, items) in grouped) {
                listItems.add(ListItem.Header(cat, items.size))
                items.forEach { listItems.add(ListItem.Tweak(it)) }
            }
        } else {
            // Flat list when searching or filtering
            filteredTweaks.forEach { listItems.add(ListItem.Tweak(it)) }
        }

        adapter.submitItems(listItems)
    }

    private fun resetAllDefaults() {
        val editor = prefs.edit()
        for (tweak in allTweaks) {
            when (tweak.defaultValue) {
                is Boolean -> editor.putBoolean(tweak.key, tweak.defaultValue)
                is String -> editor.putString(tweak.key, tweak.defaultValue)
            }
            notifyPreferenceChanged(this, tweak.key, tweak.defaultValue)
        }
        editor.apply()
        refreshList()
        Toast.makeText(this, "All tweaks reset to defaults", Toast.LENGTH_SHORT).show()
    }

    private fun openLsposed() {
        val pm = packageManager
        val lsposedPackages = arrayOf(
            "org.lsposed.manager",
            "org.lsposed.manager.nightly",
            "org.meowcat.edxposed.manager"
        )
        for (pkg in lsposedPackages) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return
            }
        }
        Toast.makeText(this, "LSPosed Manager not found! Please open LSPosed manually.", Toast.LENGTH_LONG).show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About SystemTricks")
            .setMessage("⚡ SystemTricks v3.0 (Kotlin Edition)\n\n" +
                    "A modern, high-performance system tweak module engineered for Android 10 to 17.\n\n" +
                    "• Pure Kotlin & Modern Material 3 UI\n" +
                    "• Modern LSPosed API 93 (Zero Deprecation Warnings)\n" +
                    "• Instant Real-time Live Sync (No Reboots)\n" +
                    "• Universal compatibility with HyperOS, AOSP, Pixel, LineageOS & OneUI\n\n" +
                    "Original concept & base tricks by Dark_Eyes_ (Dark Tricks).")
            .setPositiveButton("OK", null)
            .setNeutralButton("GitHub Releases") { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
                } catch (t: Throwable) {}
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        setupStatusCard()
        refreshList()
    }
}
