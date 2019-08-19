package com.example.photometa.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class PhotoData(var imagePath: String, var name: String, var size: Int, var imageWidth: Int, var imageHeight: Int): Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imagePath)
        parcel.writeString(name)
        parcel.writeInt(size)
        parcel.writeInt(imageWidth)
        parcel.writeInt(imageHeight)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PhotoData> {
        override fun createFromParcel(parcel: Parcel): PhotoData {
            return PhotoData(parcel)
        }

        override fun newArray(size: Int): Array<PhotoData?> {
            return arrayOfNulls(size)
        }
    }
}

class PhotoMetadata(var location: Location, var captureAt: Date, var deviceMetadata: DeviceMetadata)

class Location(var latitude: Double, var longitude: Double)

class DeviceMetadata(var model: String, var company: String, var len: String?)