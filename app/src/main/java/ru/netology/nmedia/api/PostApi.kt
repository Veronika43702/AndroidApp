package ru.netology.nmedia.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.User


interface ApiService {
    @GET("posts")
    suspend fun getAll(): List<Post>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(@Part file: MultipartBody.Part): Media

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun auth(@Field("login") login: String, @Field("pass") password: String): Response<User>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun register(@Field("login") login: String, @Field("pass") password: String, @Field("name") name: String): Response<User>
    @POST("users/push-tokens")
    suspend fun saveToken(@Body token: PushToken): Response<Unit>
}