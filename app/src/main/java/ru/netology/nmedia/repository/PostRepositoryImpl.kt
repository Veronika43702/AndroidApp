package ru.netology.nmedia.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import retrofit2.Response
import ru.netology.nmedia.dao.PostDao
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import java.io.IOException
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.File
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor (
    private val postDao: PostDao,
    private val apiService: ApiService
) : PostRepository {
    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders =  false),
        pagingSourceFactory = {
            PostPagingSource(
                apiService
            )
        }
    ).flow

    val postsInDB = postDao.getAll()

    override suspend fun getUnsavedPosts(): List<Post> = postDao.getUnsavedPosts().map {
        it.toDto()
    }

    override suspend fun deleteUnsavedPosts() {
        postDao.deleteUnsaved()
    }

    override suspend fun updateNewPosts() {
        postDao.updateNewPost()
    }

    override suspend fun getAll() {
        val posts: List<Post> = apiService.getAll()

        postDao.deleteUnsaved()
        postDao.insert(
            posts.map {
                PostEntity.fromDto(it)
            }
        )
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(120_000L)
            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity().map { it.copy(isNewPost = true) })
            emit(postDao.count())
        }
    }
        .catch { e -> throw AppError.from(e) }
    //  .flowOn(Dispatchers.Default)

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

            val response = apiService.save(post.copy(id = 0L))
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

    override suspend fun saveWithAttachment(post: Post, photo: PhotoModel?) {
        try {
            // если у поста id=0, то сохраняется новый пост с id на 1 меньше минимального отрицательного
            // если у поста id > 0, то сохраняется редактируемый пост
            val id = if (post.id == 0L) {
                if (postDao.findMinId() <= 0) {
                    postDao.findMinId() - 1
                } else -1
            } else post.id

            postDao.insert(PostEntity.fromDto(post.copy(id = id)))

            val postWithAttachment = if (photo != null) {
                val media = upload(photo.file)
                post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            } else {
                post
            }
            val response = apiService.save(postWithAttachment)
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

    private suspend fun upload(file: File): Media {
        val media = MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody()
        )

        return apiService.upload(media)
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            val response = apiService.removeById(id)
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
            response = if (!post.likedByMe) {
                apiService.likeById(post.id)
            } else {
                apiService.dislikeById(post.id)
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
            val response = apiService.save(post)
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