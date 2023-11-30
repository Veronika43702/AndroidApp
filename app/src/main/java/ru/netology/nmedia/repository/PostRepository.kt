package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun save(post: Post): Post
    fun removeById(id: Long)
    fun likeById(id: Long): Post
    fun share(id: Long)
}