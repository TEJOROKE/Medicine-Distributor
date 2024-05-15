package com.example.medicinedistributor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(private val cartItems: List<CartItem>, private val context: Context) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineNameTextView: TextView = itemView.findViewById(R.id.medicineNameTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val totalPriceTextView: TextView = itemView.findViewById(R.id.totalPriceTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val medicineImageView: ImageView = itemView.findViewById(R.id.imageView3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicine, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = cartItems[position]

        // Bind data to views
        holder.medicineNameTextView.text = "Medicine Name: ${currentItem.medicineName}"
        holder.quantityTextView.text = "Quantity: ${currentItem.quantity}"
        holder.totalPriceTextView.text = "Total Price : Rs. ${currentItem.totalPrice}"
        holder.statusTextView.text = "Status : ${currentItem.status}"

        // Load medicine image using Glide library
        Glide.with(context)
            .load(currentItem.medicineImageUrl)
            .into(holder.medicineImageView)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }
}
