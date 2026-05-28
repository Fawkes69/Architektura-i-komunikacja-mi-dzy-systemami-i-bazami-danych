package com.example.zarzdzanie_miejscami

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class DeskAdapter(
    private val desks: MutableList<Desk>,
    private val onReserveClick: (Desk) -> Unit
) : RecyclerView.Adapter<DeskAdapter.DeskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_desk, parent, false)
        return DeskViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeskViewHolder, position: Int) {
        holder.bind(desks[position])
    }

    override fun getItemCount(): Int = desks.size

    fun submitList(items: List<Desk>) {
        desks.clear()
        desks.addAll(items)
        notifyDataSetChanged()
    }

    inner class DeskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.desk_name)
        private val locationTextView: TextView = itemView.findViewById(R.id.desk_location)
        private val priceTextView: TextView = itemView.findViewById(R.id.desk_price)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.desk_rating)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.favorite_button)
        private val reserveButton: MaterialButton = itemView.findViewById(R.id.reserve_button)
        private val deskImage: ShapeableImageView = itemView.findViewById(R.id.desk_image)

        fun bind(desk: Desk) {
            nameTextView.text = desk.name
            locationTextView.text = desk.location
            priceTextView.text = "${desk.pricePerDay} zł"
            ratingBar.rating = desk.rating.toFloat()

            favoriteButton.setImageResource(
                if (desk.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline
            )

            favoriteButton.setOnClickListener {
                // ulubione do podpięcia później
            }

            reserveButton.setOnClickListener {
                onReserveClick(desk)
            }
        }
    }
}
