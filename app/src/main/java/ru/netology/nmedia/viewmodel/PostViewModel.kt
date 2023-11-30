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
import java.time.LocalDateTime
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    published = ""
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
        thread {
            // Начинаем загрузку
            _state.postValue(FeedModel(loading = true))
            try {
                // Данные успешно получены
                val posts = repository.getAll()
                _state.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            } catch (e: Exception) {
                // Получена ошибка
                _state.postValue(FeedModel(error = true))
            }
        }
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

    fun savePost() {
        // функция поиска ссылки на youtube и присваивание значения ссылки свойству video у поста
        //isVideoExists(content)

        // сохранение поста в репозитории
        edited.value?.let {
            thread {
                repository.save(it)
                loadPosts()
                edited.postValue(empty)
            }
        }

        // очистка edited
        edited.value = empty
    }

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

    fun removeById(id: Long) {
        thread {
            val old = _state.value?.posts.orEmpty()
            try {
                repository.removeById(id)
                loadPosts()
            } catch (e: IOException) {
                _state.postValue(FeedModel(posts = old))
                loadPosts()
            }
        }
    }

    fun likeById(id: Long) {
        thread {
            repository.likeById(id)
            loadPosts()
        }
    }

    fun share(id: Long) = repository.share(id)

    private fun isVideoExists(content: String) {
        if (content.lowercase().contains("https://www.youtu") ||
            content.lowercase().contains("https://youtu") ||
            content.lowercase().contains("http://www.youtu") ||
            content.lowercase().contains("http://youtu")
        ) {
            val partsOfContent = content.split("\\s".toRegex())
            for (part in partsOfContent) {
                if (part.lowercase().startsWith("https://www.youtu") ||
                    part.lowercase().startsWith("https://youtu") ||
                    part.lowercase().startsWith("http://www.youtu") ||
                    part.lowercase().startsWith("http://youtu")
                ) {
                    edited.value = edited.value?.copy(video = part)
                }
            }
        } else {
            edited.value = edited.value?.copy(video = "")
        }
    }
}
