package com.example.zarzdzanie_miejscami

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zarzdzanie_miejscami.network.SpaceDto
import com.google.android.material.button.MaterialButton

class AdminSpaceAdapter(
    private val items: MutableList<SpaceDto>,
    private val onEditClick: (SpaceDto) -> Unit,
    private val onDeleteClick: (SpaceDto) -> Unit
) : RecyclerView.Adapter<AdminSpaceAdapter.SpaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_space, parent, false)
        return SpaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpaceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<SpaceDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class SpaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.admin_space_title)
        private val subtitle: TextView = itemView.findViewById(R.id.admin_space_subtitle)
        private val status: TextView = itemView.findViewById(R.id.admin_space_status)
        private val editButton: MaterialButton = itemView.findViewById(R.id.edit_space_button)
        private val deleteButton: MaterialButton = itemView.findViewById(R.id.delete_space_button)

        fun bind(space: SpaceDto) {
            title.text = space.name
            subtitle.text = "Piętro ${space.floor} · ${space.spaceType.name.lowercase().replace('_', ' ')} · ${space.capacity} os."
            status.text = if (space.isAvailable) "Dostępne" else "Niedostępne"

            editButton.setOnClickListener { onEditClick(space) }
            deleteButton.setOnClickListener { onDeleteClick(space) }
        }
    }
}
