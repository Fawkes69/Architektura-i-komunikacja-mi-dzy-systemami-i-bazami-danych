package com.example.zarzdzanie_miejscami

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zarzdzanie_miejscami.network.ReservationDto
import com.google.android.material.button.MaterialButton

class ReservationAdapter(
    private val items: MutableList<ReservationDto>,
    private val onCancelClick: (ReservationDto) -> Unit
) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reservation, parent, false)
        return ReservationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<ReservationDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.reservation_title)
        private val subtitle: TextView = itemView.findViewById(R.id.reservation_subtitle)
        private val status: TextView = itemView.findViewById(R.id.reservation_status)
        private val cancelButton: MaterialButton = itemView.findViewById(R.id.cancel_reservation_button)

        fun bind(reservation: ReservationDto) {
            title.text = reservation.space?.name ?: "Miejsce #${reservation.spaceId}"
            subtitle.text = "${reservation.startTime} -> ${reservation.endTime}"
            status.text = reservation.status.name.lowercase()
            cancelButton.isEnabled = reservation.status.name == "ACTIVE"
            cancelButton.setOnClickListener { onCancelClick(reservation) }
        }
    }
}
