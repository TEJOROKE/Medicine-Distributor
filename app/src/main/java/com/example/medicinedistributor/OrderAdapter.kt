package com.example.medicinedistributor


import android.content.Context // Import Context class
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase

class OrderAdapter(private val context: Context, private val orderItems: List<OrderItem>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(orderItem: OrderItem)
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shopOwnerNameTextView: TextView = itemView.findViewById(R.id.shopOwnerNameTextView)
        val shopOwnerAddressTextView: TextView = itemView.findViewById(R.id.shopOwnerAddressTextView)
        val shopOwnerPhoneNumberTextView: TextView = itemView.findViewById(R.id.shopOwnerPhoneNumberTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val totalPriceTextView: TextView = itemView.findViewById(R.id.totalPriceTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val medicineImageView: ImageView = itemView.findViewById(R.id.medicineImageView)
        val buttonChangeStatus: Button = itemView.findViewById(R.id.buttonChangeStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val currentItem = orderItems[position]

        // Bind data to views
        holder.shopOwnerNameTextView.text = "Shop Owner: ${currentItem.shopOwnerName}"
        holder.shopOwnerAddressTextView.text = "Address: ${currentItem.shopOwnerAddress}"
        holder.shopOwnerPhoneNumberTextView.text = "Phone: ${currentItem.shopOwnerPhoneNumber}"
        holder.quantityTextView.text = "Quantity: ${currentItem.quantity}"
        holder.totalPriceTextView.text = "Total Price: Rs. ${currentItem.totalAmount}"
        holder.statusTextView.text = "Status: ${currentItem.status}"

        // Load medicine image using Glide
        Glide.with(context).load(currentItem.imageUrl).into(holder.medicineImageView)

        // Set click listener for the button
        holder.buttonChangeStatus.setOnClickListener {
            // Call a method to handle status change
            handleChangeStatus(currentItem)
        }
    }

    private fun handleChangeStatus(currentItem: OrderItem) {
        // Create a list of status options
        val statusOptions = arrayOf("Processing Order", "Packed Order", "Ready to Deliver", "On the Way", "Delivered")

        // Show a dialog with the status options
        MaterialAlertDialogBuilder(context)
            .setTitle("Change Order Status")
            .setItems(statusOptions) { dialog, which ->
                val selectedStatus = statusOptions[which]

                // Show confirmation dialog before updating status
                MaterialAlertDialogBuilder(context)
                    .setTitle("Confirm Status Change")
                    .setMessage("Are you sure you want to change the status to \"$selectedStatus\"?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Update the status field in the database
                        val database = FirebaseDatabase.getInstance()
                        val orderRef = database.getReference("orders").child(currentItem.orderId)

                        // Update the status field with selected status
                        orderRef.child("status").setValue(selectedStatus)
                            .addOnSuccessListener {
                                // Show toast for successful status update
                                Toast.makeText(context, "Order status updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                // Show toast for status update failure
                                Toast.makeText(context, "Failed to update order status: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
            .show()
    }

    override fun getItemCount(): Int {
        return orderItems.size
    }
}
