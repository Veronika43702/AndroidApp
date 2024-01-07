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
    override val data: LiveData<List<Post>> =
        postDao.getAll().map {
            it.map { post ->
                post.toDto()
            }
        }

    override suspend fun getUnsavedPosts(): List<Post> = postDao.getUnsavedPosts().map {
        it.toDto()
    }

    override suspend fun deleteUnsavedPosts() {
        postDao.deleteUnsaved()
    }

    override suspend fun getAll() {
        val posts: List<Post> = PostsApi.retrofitService.getAll()

        postDao.deleteUnsaved()
        postDao.insert(
            posts.map {
                PostEntity.fromDto(it)
            }
        )
    }

    override suspend fun save(post: Post) {
        try {
            // если у поста id=0, то сохраняется новый пост с id на 1 меньше минимального отрицательного
            // если у поста id > 0, то сохраняется редактируемый пост
            val id = if (post.id == 0L) {
                if (postDao.findMinId() <= 0) {
                    postDao.findMinId() - 1
                } else -1
            } else post.id

            postDao.insert(PostEntity.fromDto(post.copy(id = id)))

            val response = PostsApi.retrofitService.save(post.copy(id = 0L))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            // удаляем из местной базы пост с отрицательным id (несохраненный до ответа от сервера)
            postDao.removeById(id)
            // сохраняем пост, полученный от сервера
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

    override suspend fun saveEditedPost(post: Post) {
        try {
            postDao.insert(PostEntity.fromDto(post))
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
}