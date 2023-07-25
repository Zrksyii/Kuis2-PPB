package com.akmalzarkasyi.sharedpreferenced

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SettingModel(
    var name: String? = null,
    var address: String? = null,
    var gender: String? = null,
    var email: String? = null,
    var age: Int = 0,
    var phoneNumber: String? = null,
    var isDarkTheme: Boolean = false
) : Parcelable