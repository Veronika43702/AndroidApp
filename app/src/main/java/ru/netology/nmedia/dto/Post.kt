package ru.netology.nmedia.dto

data class Post (
    val id: Long = 0,
    val author: String,
    val authorAvatar: String = "",
    val content: String,
    val published: Long = 0L,
    val likedByMe: Boolean = false,
    val likes: Int = 0,
    val share: Int = 0,
    val views: Int = 0,
    var attachment: Attachment? = null,
    //val video: String = "",
)

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

enum class AttachmentType {
    IMAGE
}