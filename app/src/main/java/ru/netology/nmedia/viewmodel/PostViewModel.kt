package ru.netology.nmedia.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInMemoryImpl

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    published = ""
)

class PostViewModel : ViewModel() {

    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()
    val edited = MutableLiveData(empty)
    //val binding: ActivityMainBinding


    fun save() {
        edited.value?.let {
            repository.save(it)
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        //binding.group.visibility = View.VISIBLE
        edited.value = post
    }

    fun cancelEdit(post: Post){
        edited.value = empty
    }

    fun changeContent(content: String) {
        edited.value?.let {
            val text = content.trim()
            if (it.content != text) {
                edited.value = it.copy(content = text)
            }
        }
    }



    fun removeById(id: Long) = repository.removeById(id)

    fun likeById(id: Long) = repository.likeById(id)

    fun share(id: Long) = repository.share(id)


}