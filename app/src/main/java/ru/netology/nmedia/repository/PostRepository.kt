package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun save(post: Post)
    fun removeById(id: Long)
    fun likeById(id: Long)
    fun share(id: Long)
}