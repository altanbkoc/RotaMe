package com.altankoc.pinnedplaces.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = "places")
data class Place(

    @ColumnInfo(name = "name")
    var name:String,

    @ColumnInfo(name = "latitude")
    var latitude: Double,

    @ColumnInfo(name = "longitude")
    var longitude: Double,

    @ColumnInfo(name = "placeImage")
    var placeImage: ByteArray
):Parcelable
{

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

}