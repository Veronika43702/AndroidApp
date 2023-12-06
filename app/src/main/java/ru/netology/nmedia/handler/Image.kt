package ru.netology.nmedia.handler

import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R


fun ImageView.load(url: String, timeout: Int = 30_000){
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .circleCrop()
        .into(this)
}