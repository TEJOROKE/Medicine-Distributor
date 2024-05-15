package com.example.medicinedistributor

import android.os.Parcel
import android.os.Parcelable

data class User(
    val shopName: String? = null,
    val phoneNumber: String? = null,
    val shopAddress: String? = null,
    // Add other fields if needed
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(shopName)
        parcel.writeString(phoneNumber)
        parcel.writeString(shopAddress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}