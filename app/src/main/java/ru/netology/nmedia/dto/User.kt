package ru.netology.nmedia.dto

data class User(
    val id: Long,
    val token: String,
    val name: String
)

data class PushToken(val token: String)