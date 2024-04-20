package com.ifs21025.lostandfound.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DelcomLostandFound(
    val id: Int,
    val title: String,
    val description: String,
    var isCompleted: Boolean,
    val status: String,
    val cover: String?,
) : Parcelable