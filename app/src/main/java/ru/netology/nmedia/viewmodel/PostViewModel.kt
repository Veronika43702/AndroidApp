package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositorySQLiteImpl
import java.time.LocalDateTime

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositorySQLiteImpl(
        AppDb.getInstance(application).postDao
    )
    val data = repository.getAll()
    val edited = MutableLiveData(empty)
    private var draft: String = ""

    // функция сохранения изменений
    fun saveNewPost(content: String) {
        // trim = обрезка пробелов в конце/спереди
        val text = content.trim()
        // если изменений не было: очистка edited, выход из фукнции (нужен ли это код?)
        if (edited.value?.content == text) {
            edited.value = empty
            return
        }
        // наполнение данными нового поста
        edited.value = edited.value?.copy(
            author = "me",
            content = text,
            published = LocalDateTime.now().toString()
        )
        // функция поиска ссылки на youtube и присваивание значения ссылки свойству video у поста
        isVideoExists(content)

        // сохранение поста
        edited.value?.let {
            repository.save(it)
        }
        // очистка edited
        edited.value = empty
    }

    // функция редактирования (edited = редактируемый пост)
    fun edit(post: Post) {
        edited.value = post
    }

    // функция сохранения изменений
    fun editSave(content: String) {
        // trim = обрезка пробелов в конце/спереди
        val text = content.trim()
        // если изменений не было: очистка edited, выход из фукнции
        if (edited.value?.content == text) {
            edited.value = empty
            return
        }
        // сохранение нового текста (text) в содержимое поста (content) через функцию сохранения в PostRepository (File, In Memory)
        edited.value = edited.value?.copy(content = text)
        // функция поиска ссылки на youtube и присваивание значения ссылки свойству video у поста
        isVideoExists(content)

        // сохранение поста в репозитории
        edited.value?.let {
            repository.save(it)
        }

        // очистка edited
        edited.value = empty
    }

    fun cancelEdit() {
        edited.value = empty
    }

    fun cancelSave(content: String) {
        draft = content
    }

    fun clearDraft() {
        draft = ""
    }

    fun getDraft(): String {
        return draft
    }

    fun removeById(id: Long) = repository.removeById(id)

    fun likeById(id: Long) = repository.likeById(id)

    fun share(id: Long) = repository.share(id)

    private fun isVideoExists(content: String) {
        if (content.lowercase().contains("https://www.youtube.com/") || content.lowercase().contains("https://youtube.com/")) {
            val partsOfContent = content.split("\\s".toRegex())
            for (part in partsOfContent) {
                if (part.lowercase().startsWith("https://www.youtube.com/") ||
                    part.lowercase().startsWith("https://youtube.com/")
                ) {
                    edited.value = edited.value?.copy(video = part)
                }
            }
        } else {
            edited.value = edited.value?.copy(video = "")
        }
    }
}
