package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import retrofit2.Response
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import java.io.IOException
import ru.netology.nmedia.error.UnknownError

class PostRepositoryImpl(
    private val postDao: PostDao
) : PostRepository {
    override val data: LiveData<List<Post>> = postDao.getAll().map {
        it.map { post ->
            post.toDto()
        }
    }

    override suspend fun getAll() {
        val posts: List<Post> = PostsApi.retrofitService.getAll()

        postDao.insert(
            posts.map {
                PostEntity.fromDto(it)
            }
        )
    }

    override suspend fun save(post: Post) {
        try {
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(post: Post) {
        val response: Response<Post>
        postDao.likeById(post.id)
        try {
            if (!post.likedByMe) {
                response = PostsApi.retrofitService.likeById(post.id)
            } else {
                response = PostsApi.retrofitService.dislikeById(post.id)
            }

            if (!response.isSuccessful) {
                postDao.likeById(post.id)
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            postDao.likeById(post.id)
            throw NetworkError
        } catch (e: Exception) {
            postDao.likeById(post.id)
            throw UnknownError
        }
    }

//
//    override fun likeById(post: Post, callback: PostRepository.Callback<Post>) {
//        if (!post.likedByMe) {
//            PostApiService.service.like(post.id)
//                .enqueue(object : Callback<Post> {
//                    override fun onFailure(call: Call<Post>, t: Throwable) {
//                        callback.onError(Exception(t))
//                    }
//
//                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
//                        if (!response.isSuccessful){
//                            callback.onError(Exception(response.errorBody()?.string()))
//                            return
//                        }
//
//                        try {
//                            val body = response.body() ?: throw RuntimeException("body is null")
//                            callback.onSuccess(body)
//                        } catch (e: Exception) {
//                            callback.onError(e)
//                        }
//                    }
//                })
//        } else {
//            PostApiService.service.unlike(post.id)
//                .enqueue(object : Callback<Post> {
//                    override fun onFailure(call: Call<Post>, t: Throwable) {
//                        callback.onError(Exception(t))
//                    }
//
//                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
//                        if (!response.isSuccessful){
//                            callback.onError(Exception(response.errorBody()?.string()))
//                            return
//                        }
//
//
//                        try {
//                            val body = response.body() ?: throw RuntimeException("body is null")
//                            callback.onSuccess(body)
//                        } catch (e: Exception) {
//                            callback.onError(e)
//                        }
//                    }
//                })
//        }
//    }
}