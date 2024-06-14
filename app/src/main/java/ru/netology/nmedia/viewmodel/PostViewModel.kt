package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.time.OffsetDateTime

private val empty = Post(
        authorId = 0,
        content = "",
        author = "you "

)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
            PostRepositoryImpl(AppDb.getInstance(application).postDao())

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: LiveData<FeedModel> = AppAuth.getInstance()
            .authState
            .flatMapLatest { auth ->
                repository.data.map { posts ->
                    FeedModel(
                            posts.map { it.copy(ownedByMe = auth.id == it.authorId)},
                            posts.isEmpty()
                    )
                }
            }
            .catch { it.printStackTrace() }
            .asLiveData(Dispatchers.Default)

    val newerCount: LiveData<Int> = data.switchMap {
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
                .catch { _state.postValue(FeedModelState(error = true)) }
                .asLiveData(Dispatchers.Default, 100)
    }

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    private val edited = MutableLiveData(empty)
    private var draft: String = ""

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        deleteUnsavedPosts()
        loadPosts()
    }

    fun deleteUnsavedPosts() {
        viewModelScope.launch {
            repository.deleteUnsavedPosts()
        }
    }


    fun loadPosts() {
        _state.value = (FeedModelState(loading = true))
        viewModelScope.launch {
            try {
                repository.getAll()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun updateNewPost() {
        viewModelScope.launch {
            repository.updateNewPosts()
        }
    }

    fun refresh() {
        _state.value = (FeedModelState(refreshing = true))
        viewModelScope.launch {
            try {
                repository.getAll()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun loadUnsavedPosts() {
        viewModelScope.launch {
            repository.getUnsavedPosts().map {
                try {
                    repository.save(it)
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(errorOfSave = true)
                }
            }
        }
    }

    fun save() {
        edited.value?.copy(published = OffsetDateTime.now().toEpochSecond()).let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    it?.let { post -> repository.saveWithAttachment(post, _photo.value) }
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(errorOfSave = true)
                }
            }
        }
        edited.value = empty
    }

    fun savePhoto(photoModel: PhotoModel) {
        _photo.value = photoModel
    }

    fun clearPhoto() {
        _photo.value = null
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(errorOfDelete = true, id = id)
            }
        }
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                repository.likeById(post)
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(errorOfLike = true, post = post)

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

        edited.value?.copy(content = text).let {
            viewModelScope.launch {
                try {
                    it?.let { post -> repository.saveEditedPost(post) }
                    _state.value = FeedModelState()
                } catch (e: Exception) {
                    _state.value = FeedModelState(errorOfEdit = true)
                }
            }
        }
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
}
