package com.github.billman64.nycschoolssatscores.Model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class School(var dbn:String, var schoolName:String): Parcelable{

    @SerializedName("dbn")
    @Expose
    var mDbn:String
        internal set

    @SerializedName("school_name")
    @Expose
    var mSchoolName:String
        internal set

    constructor(parcel:Parcel):this(
        parcel.readString()?:"no string found",
        parcel.readString().toString()
    )

    init {
        mDbn = dbn
        mSchoolName = schoolName
    }


//    override fun writeToParcel(dest: Parcel?, flags: Int) {
//        dest?.writeString(dbn)
//        dest?.writeString(schoolName)
//    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0?.writeString(dbn)
        p0?.writeString(schoolName)
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    companion object CREATOR: Parcelable.Creator<School>{
        override fun createFromParcel(source: Parcel): School {
            return School(source)
        }

        override fun newArray(size: Int): Array<School?> {
            return arrayOfNulls(size)
        }
    }
}