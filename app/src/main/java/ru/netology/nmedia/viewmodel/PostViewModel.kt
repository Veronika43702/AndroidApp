package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    published = "",
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _state = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _state
    val edited = MutableLiveData(empty)
    private var draft: String = ""
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }


    fun loadPosts() {
        _state.value = (FeedModel(loading = true))
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(data: List<Post>) {
                _state.postValue(FeedModel(posts = data, empty = data.isEmpty()))
            }

            override fun onError(e: Exception) {
                _state.postValue(FeedModel(error = true))
            }
        })
    }

    fun removeByIdAsync(id: Long) {
        val old = _state.value?.posts.orEmpty()
        repository.removeByIdAsync(id, object : PostRepository.Callback<Long> {
            override fun onSuccess(data: Long) {
                _state.postValue(FeedModel(posts = _state.value?.posts.orEmpty()
                        .filter { it.id != id }
                    )
                )
            }

            override fun onError(e: Exception) {
                _state.postValue(FeedModel(posts = old))
            }
        })
    }

    fun savePostAsync() {
        val posts = _state.value?.posts.orEmpty()
        _state.value = (FeedModel(loading = true))
        // сохранение поста в репозитории
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.Callback<Post> {
                override fun onSuccess(data: Post) {
                    _state.postValue(FeedModel(posts = listOf(data) + posts)
                    )
                }
                override fun onError(e: Exception) {
                    _state.postValue(FeedModel(error = true))
                }
            })
            edited.postValue(empty)

        }
    }

    fun likeByPostAsync(post: Post) {
            repository.likeByPostAsync(post, object : PostRepository.Callback<Post>{
                override fun onSuccess(data: Post) {
                    _state.postValue(FeedModel(posts = _state.value?.posts.orEmpty()
                        .map{ if (it.id == post.id) data else it }))
                }

                override fun onError(e: Exception) {
                    _state.postValue(FeedModel(error = true))
                }
            })
        }

    // функция наполнения и сохранения нового поста
    fun configureNewPost(content: String) {
        // trim = обрезка пробелов в конце/спереди
        val text = content.trim()
        // если изменений не было: очистка edited, выход из фукнции (нужен ли это код?)
        if (edited.value?.content == text) {
            edited.value = empty
            return
        }
        // наполнение данными нового поста
        edited.value = edited.value?.copy(content = text)
    }

    // функция редактирования (edited = редактируемый пост)
    fun edit(post: Post) {
        edited.value = post
    }

    // функция сохранения изменений при редактировании поста
    fun editPost(content: String) {
        // trim = обрезка пробелов в конце/спереди
        val text = content.trim()
        // если изменений не было: очистка edited, выход из фукнции
        if (edited.value?.content == text) {
            edited.value = empty
            return
        }
        // сохранение нового текста (text) в содержимое поста (content) через функцию сохранения в PostRepository (File, In Memory)
        edited.value = edited.value?.copy(content = text)
    }

//    fun savePost() {
//        // функция поиска ссылки на youtube и присваивание значения ссылки свойству video у поста
//        // isVideoExists(content)
//
//        // сохранение поста в репозитории
//        edited.value?.let {
//            thread {
//                repository.save(it)
//                //loadPosts()
//                edited.postValue(empty)
//            }
//        }
//    }

    fun cancelEdit() {
        edited.value = empty
    }

    // функции для отмены сохранения нового поста и сохранения черновика для отображения текста черновика при создании нового поста
    fun cancelSave(content: String) {
        draft = content
    }

    fun clearDraft() {
        draft = ""
    }

    fun getDraft(): String {
        return draft
    }

//    fun removeById(id: Long) {
//        thread {
//            val old = _state.value?.posts.orEmpty()
//            _state.postValue(
//                _state.value?.copy(posts = _state.value?.posts.orEmpty()
//                    .filter { it.id != id }
//                )
//            )
//            try {
//                repository.removeById(id)
//            } catch (e: IOException) {
//                _state.postValue(
//                    _state.value?.copy(posts = old)
//                )
//            }
//        }
//    }
//
//    fun likeById(id: Long) {
//        thread {
//            val postLiked = repository.likeById(id)
//            _state.postValue(
//                _state.value?.copy(posts = _state.value?.posts.orEmpty()
//                    .map { if (it.id == id) postLiked else it })
//            )
//
//        }
//    }

    fun share(id: Long) = repository.share(id)

//    private fun isVideoExists(content: String) {
//        if (content.lowercase().contains("https://www.youtu") ||
//            content.lowercase().contains("https://youtu") ||
//            content.lowercase().contains("http://www.youtu") ||
//            content.lowercase().contains("http://youtu")
//        ) {
//            val partsOfContent = content.split("\\s".toRegex())
//            for (part in partsOfContent) {
//                if (part.lowercase().startsWith("https://www.youtu") ||
//                    part.lowercase().startsWith("https://youtu") ||
//                    part.lowercase().startsWith("http://www.youtu") ||
//                    part.lowercase().startsWith("http://youtu")
//                ) {
//                    edited.value = edited.value?.copy(video = part)
//                }
//            }
//        } else {
//            edited.value = edited.value?.copy(video = "")
//        }
//    }
}
