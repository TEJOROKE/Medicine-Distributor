package com.example.medicinedistributor
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide



class AdapterHome(
    private val context: Context,
    private var medicineList: List<Medicine>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AdapterHome.ViewHolder>() {

    private var originalMedicineList: List<Medicine> = mutableListOf()

    init {
        originalMedicineList = medicineList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.carddesign, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicineList[position]

        holder.medName.text = medicine.name
        holder.brandName.text = medicine.manufacturer
        holder.price.text = "Rs. ${medicine.price}"
        holder.quantity.text = medicine.quantity.toString()

        Glide.with(context)
            .load(medicine.imageUrl)
            .placeholder(R.drawable.baseline_insert_photo_24)
            .error(R.drawable.baseline_home_24) // Set error placeholder or handle error
            .into(holder.imageView)
        holder.itemView.setOnClickListener {
            listener.onItemClick(medicine)
        }
    }

    override fun getItemCount(): Int {
        return medicineList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medName: TextView = itemView.findViewById(R.id.medname)
        val brandName: TextView = itemView.findViewById(R.id.brandname)
        val price: TextView = itemView.findViewById(R.id.price)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    fun filter(query: String) {
        medicineList = if (query.isEmpty()) {
            originalMedicineList
        } else {
            originalMedicineList.filter { medicine ->
                medicine.name?.contains(query, ignoreCase = true) ?: false
            }
        }
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(medicine: Medicine)
    }
}
