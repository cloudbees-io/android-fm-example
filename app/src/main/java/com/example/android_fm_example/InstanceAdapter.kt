package com.example.android_fm_example

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class InstanceItem(
    val sdkKey: String,
    val value: String,
    val isVisible: Boolean = true,
    val fontColor: String = "black",
    val fontSize: Int = 16
)

class InstanceAdapter : RecyclerView.Adapter<InstanceAdapter.ViewHolder>() {
    private var instances = mutableListOf<InstanceItem>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.instanceTitle)
        val valueText: TextView = view.findViewById(R.id.instanceValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instance = instances[position]
        holder.titleText.text = instance.sdkKey
        holder.valueText.apply {
            text = instance.value
            visibility = if (instance.isVisible) View.VISIBLE else View.GONE
            setTextColor(getColorFromFlag(instance.fontColor))
            textSize = instance.fontSize.toFloat()
        }
    }

    private fun getColorFromFlag(color: String): Int {
        return when (color.lowercase()) {
            "red" -> Color.RED
            "blue" -> Color.BLUE
            "green" -> Color.GREEN
            "yellow" -> Color.YELLOW
            else -> Color.BLACK
        }
    }

    override fun getItemCount() = instances.size

    fun updateInstances(newInstances: List<InstanceItem>) {
        instances.clear()
        instances.addAll(newInstances)
        notifyDataSetChanged()
    }
}
