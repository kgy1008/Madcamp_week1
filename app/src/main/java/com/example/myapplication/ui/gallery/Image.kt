package com.example.myapplication.ui.gallery

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image (
    var imageSrc : Uri
) : Parcelable
