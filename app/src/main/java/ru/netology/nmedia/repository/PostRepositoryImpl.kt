package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.lang.RuntimeException

class PostRepositoryImpl : PostRepository {
    // Сетевой вызов с тайм-аутом 30 секунд для обработки

    override fun getAll(): List<Post> {
        return PostApiService.service.getAll()
            .execute()
            .let{
                it.body() ?: throw RuntimeException("body is null")
            }
    }

    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        PostApiService.service.getAll()
            .enqueue(object : Callback<List<Post>> {
              override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callback.onError(Exception(t))
                }

                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                   if (!response.isSuccessful){
                       callback.onError(Exception(response.errorBody()?.string()))
                       return
                   }

                    try {
                        val body = response.body() ?: throw RuntimeException("body is null")
                        callback.onSuccess(body)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            })
    }

    override fun removeById(id: Long, callback: PostRepository.Callback<Unit>) {
        PostApiService.service.deletePostById(id)
            .enqueue(object : Callback<Unit> {
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(Exception(t))
                }

                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful){
                        callback.onError(Exception(response.errorBody()?.string()))
                        return
                    }
                    callback.onSuccess(Unit)
                }
            })
    }

    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        PostApiService.service.save(post)
            .enqueue(object : Callback<Post> {
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }

                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful){
                        callback.onError(Exception(response.errorBody()?.string()))
                        return
                    }

                    try {
                        val body = response.body() ?: throw RuntimeException("body is null")
                        callback.onSuccess(body)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            })
    }

    override fun likeById(post: Post, callback: PostRepository.Callback<Post>) {
        if (!post.likedByMe) {
            PostApiService.service.like(post.id)
                .enqueue(object : Callback<Post> {
                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callback.onError(Exception(t))
                    }

                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (!response.isSuccessful){
                            callback.onError(Exception(response.errorBody()?.string()))
                            return
                        }

                        try {
                            val body = response.body() ?: throw RuntimeException("body is null")
                            callback.onSuccess(body)
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }
                })
        } else {
            PostApiService.service.unlike(post.id)
                .enqueue(object : Callback<Post> {
                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callback.onError(Exception(t))
                    }

                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (!response.isSuccessful){
                            callback.onError(Exception(response.errorBody()?.string()))
                            return
                        }


                        try {
                            val body = response.body() ?: throw RuntimeException("body is null")
                            callback.onSuccess(body)
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }
                })
        }
    }

    override fun share(id: Long) {
        // TODO: do this in homework
    }
}