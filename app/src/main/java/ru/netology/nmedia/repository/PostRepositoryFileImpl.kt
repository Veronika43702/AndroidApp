package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositoryFileImpl(private val context: Context) : PostRepository {
    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val filename = "posts.json"
    private var nextId = 1L
    private var posts = emptyList<Post>()
        set(value) {
            field = value
            sync()
        }
    private val data = MutableLiveData(posts)

    init {
        val file = context.filesDir.resolve(filename)
        if (file.exists()) {
            context.openFileInput(filename).bufferedReader().use {
                posts = gson.fromJson(it, type)
                if (posts.isEmpty()){
                    nextId = 1L
                } else {
                    nextId = posts.maxOf { it.id } + 1
                }
                data.value = posts
            }
        }
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun save(post: Post) {
        if (post.id == 0L) {
            posts = listOf(
                post.copy(
                    id = nextId++,
                    author = "Me",
                    likedByMe = false,
                    published = "now",
                    share = 0,
                    likes = 0,
                    views = 0
                )
            ) + posts
        } else {
            posts = posts.map { if (it.id != post.id) it else it.copy(content = post.content) }
        }
        data.value = posts
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
    }

    override fun likeById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likedByMe = !it.likedByMe,
                likes = if (it.likedByMe) it.likes - 1 else it.likes + 1
            )
        }
        data.value = posts
    }

    override fun share(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(share = it.share + 1)
        }
        data.value = posts
    }

    private fun sync() {
        context.openFileOutput(filename, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts))
        }
    }

}