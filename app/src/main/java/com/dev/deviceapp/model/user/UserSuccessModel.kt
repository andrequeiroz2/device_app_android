package com.dev.deviceapp.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserSuccess(
    val uuid: String,
    val username: String,
    val email: String,
) : Parcelable