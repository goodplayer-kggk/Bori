package com.goodground.bori.ui.photo.editor

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goodground.bori.R

class TintPresetAdapter(
    private val presets: List<TintPreset>,
    private val onClick: (TintPreset) -> Unit
) : RecyclerView.Adapter<TintPresetAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val colorView: View = view.findViewById(R.id.viewColor)
        val nameText: TextView = view.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tint_preset, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val preset = presets[position]

        holder.nameText.text = preset.name
        holder.colorView.backgroundTintList =
            ColorStateList.valueOf(preset.color)

        // 선택 표시
        holder.colorView.scaleX = if (position == selectedPosition) 1f else 0.7f
        holder.colorView.scaleY = if (position == selectedPosition) 1f else 0.7f

        holder.itemView.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = position
            notifyItemChanged(prev)
            notifyItemChanged(position)
            onClick(preset)
        }
    }

    override fun getItemCount(): Int = presets.size
}