package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int,
    val share: Int,
    val views: Int,
    //val video: String,
){
    fun toDto(): Post  = Post(
        id = id,
        author = author,
        published = published,
        content = content,
        likedByMe = likedByMe,
        likes = likes,
        share = share,
        views = views
        //video = video
    )

    companion object{
        fun fromDto(dto: Post): PostEntity = with(dto){
            PostEntity(
                id = id,
                author = author,
                published = published,
                content = content,
                likedByMe = likedByMe,
                likes = likes,
                share = share,
                views = views
                //video = video
            )
        }
    }
}