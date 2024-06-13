package ru.netology.nmedia.model

data class AuthModel (
    val wrongData: Boolean = false,
    val networkError: Boolean = false,
    val unableSingIn: Boolean = false
)