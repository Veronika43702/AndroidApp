package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity (
    @PrimaryKey
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int,
    val share: Int,
    val views: Int,
    var isNewPost: Boolean,
    @Embedded
    val attachment: AttachmentEmb? = null
    //val video: String,
){
    fun toDto(): Post  = Post(id,authorId,author,authorAvatar,content,published,likedByMe,likes, share, views, attachment?.toDto())

    companion object{
        fun fromDto(dto: Post): PostEntity = with(dto){
            PostEntity(id,authorId, author, authorAvatar, content, published,likedByMe, likes, share, views, false, AttachmentEmb.fromDto(attachment))
        }
    }
}

data class AttachmentEmb(
        val url: String,
        val type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object{
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmb(it.url, it.type)
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)