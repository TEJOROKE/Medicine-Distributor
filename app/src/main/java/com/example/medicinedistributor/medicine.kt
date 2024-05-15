package com.example.medicinedistributor
import android.os.Parcel
import android.os.Parcelable

data class Medicine(
    val id: String? = null,
    val name: String? = null,
    val price: Double? = null,
    val manufacturer: String? = null,
    val quantity: Int? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    var distributorId: String? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeValue(price)
        parcel.writeString(manufacturer)
        parcel.writeValue(quantity)
        parcel.writeString(description)
        parcel.writeString(imageUrl)
        parcel.writeString(distributorId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Medicine> {
        override fun createFromParcel(parcel: Parcel): Medicine {
            return Medicine(parcel)
        }

        override fun newArray(size: Int): Array<Medicine?> {
            return arrayOfNulls(size)
        }
    }
}