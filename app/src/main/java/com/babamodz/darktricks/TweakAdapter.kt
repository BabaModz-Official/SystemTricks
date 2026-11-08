package com.babamodz.darktricks

import android.content.Context
import android.content.SharedPreferences
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

class TweakAdapter(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val onPrefChanged: (key: String, value: Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TWEAK = 1
    }

    private var items: List<ListItem> = emptyList()

    fun submitItems(newItems: List<ListItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.Tweak -> TYPE_TWEAK
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val view = inflater.inflate(R.layout.item_category_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_tweak_card, parent, false)
            TweakViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is ListItem.Tweak -> (holder as TweakViewHolder).bind(item.def)
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_category_title)
        private val tvCount: TextView = itemView.findViewById(R.id.tv_category_count)

        fun bind(header: ListItem.Header) {
            tvTitle.text = "${header.category.icon}  ${header.category.title}"
            tvCount.text = "${header.count}"
        }
    }

    inner class TweakViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.card_tweak)
        private val tvIcon: TextView = itemView.findViewById(R.id.tv_icon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvSummary: TextView = itemView.findViewById(R.id.tv_summary)
        private val switchTweak: MaterialSwitch = itemView.findViewById(R.id.switch_tweak)
        private val chipValue: TextView = itemView.findViewById(R.id.chip_value)

        fun bind(def: TweakDef) {
            tvIcon.text = def.icon
            tvTitle.text = def.title
            tvSummary.text = def.summary

            when (def.type) {
                TweakType.SWITCH -> {
                    chipValue.visibility = View.GONE
                    switchTweak.visibility = View.VISIBLE

                    val currentVal = prefs.getBoolean(def.key, def.defaultValue as Boolean)
                    switchTweak.setOnCheckedChangeListener(null)
                    switchTweak.isChecked = currentVal

                    switchTweak.setOnCheckedChangeListener { _, isChecked ->
                        card.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        prefs.edit().putBoolean(def.key, isChecked).apply()
                        onPrefChanged(def.key, isChecked)
                    }

                    card.setOnClickListener {
                        switchTweak.toggle()
                    }
                }
                TweakType.LIST -> {
                    switchTweak.visibility = View.GONE
                    chipValue.visibility = View.VISIBLE

                    val currentVal = prefs.getString(def.key, def.defaultValue as String) ?: def.defaultValue as String
                    val index = def.entryValues?.indexOf(currentVal) ?: -1
                    val currentLabel = if (index in 0 until (def.entries?.size ?: 0)) {
                        def.entries!![index]
                    } else {
                        "Disabled"
                    }
                    chipValue.text = "$currentLabel  ▼"

                    val clickListener = View.OnClickListener {
                        card.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        showListSelectionDialog(def)
                    }
                    card.setOnClickListener(clickListener)
                    chipValue.setOnClickListener(clickListener)
                }
                TweakType.TEXT -> {
                    switchTweak.visibility = View.GONE
                    chipValue.visibility = View.VISIBLE

                    val currentText = prefs.getString(def.key, def.defaultValue as String) ?: ""
                    chipValue.text = if (currentText.isBlank()) "Default  ✏️" else "$currentText  ✏️"

                    val clickListener = View.OnClickListener {
                        card.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        showTextInputDialog(def)
                    }
                    card.setOnClickListener(clickListener)
                    chipValue.setOnClickListener(clickListener)
                }
            }
        }

        private fun showListSelectionDialog(def: TweakDef) {
            val entries = def.entries ?: return
            val values = def.entryValues ?: return

            val currentVal = prefs.getString(def.key, def.defaultValue as String) ?: def.defaultValue as String
            val selectedIndex = values.indexOf(currentVal).coerceAtLeast(0)

            MaterialAlertDialogBuilder(context)
                .setTitle("${def.icon}  ${def.title}")
                .setSingleChoiceItems(entries, selectedIndex) { dialog, which ->
                    val chosenValue = values[which]
                    prefs.edit().putString(def.key, chosenValue).apply()
                    onPrefChanged(def.key, chosenValue)
                    chipValue.text = "${entries[which]}  ▼"
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun showTextInputDialog(def: TweakDef) {
            val currentText = prefs.getString(def.key, def.defaultValue as String) ?: ""
            val input = EditText(context).apply {
                setText(currentText)
                setSingleLine()
                setSelection(currentText.length)
                hint = "Leave empty for default carrier text"
            }

            val container = FrameLayout(context).apply {
                setPadding(50, 20, 50, 10)
                addView(input)
            }

            MaterialAlertDialogBuilder(context)
                .setTitle("${def.icon}  ${def.title}")
                .setMessage(def.summary)
                .setView(container)
                .setPositiveButton("Save") { _, _ ->
                    val newText = input.text.toString().trim()
                    prefs.edit().putString(def.key, newText).apply()
                    onPrefChanged(def.key, newText)
                    chipValue.text = if (newText.isBlank()) "Default  ✏️" else "$newText  ✏️"
                }
                .setNeutralButton("Clear") { _, _ ->
                    prefs.edit().putString(def.key, "").apply()
                    onPrefChanged(def.key, "")
                    chipValue.text = "Default  ✏️"
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
