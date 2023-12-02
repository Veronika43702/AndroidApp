package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAllAsync(callback: Callback<List<Post>>)
    fun saveAsync(post: Post, callback: Callback<Post>)
    fun removeByIdAsync(id: Long, callback: Callback<Long>)
    fun likeByPostAsync(post: Post, callback: Callback<Post>)

//    fun getAll(): List<Post>
//    fun save(post: Post)
//    fun removeById(id: Long)
//    fun likeById(id: Long): Post
    fun share(id: Long)

    interface Callback<T>{
        fun onSuccess(data: T) {}
        fun onError(e: Exception) {}
    }
}