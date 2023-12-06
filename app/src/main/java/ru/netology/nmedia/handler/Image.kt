package ru.netology.nmedia.handler

import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R


fun ImageView.loadAvatars(url: String, timeout: Int = 30_000){
    Glide.with(this)
        .load(url)
        .timeout(timeout)
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .circleCrop()
        .into(this)
}

fun ImageView.loadAttachment(url: String, timeout: Int = 30_000){
    Glide.with(this)
        .load(url)
        .timeout(timeout)
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .into(this)
}