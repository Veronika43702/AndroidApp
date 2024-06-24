package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel(
    private val appAuth: AppAuth
): ViewModel() {
    val data = appAuth.authState

    val authenticated: Boolean
        get() = data.value.id != 0L
}