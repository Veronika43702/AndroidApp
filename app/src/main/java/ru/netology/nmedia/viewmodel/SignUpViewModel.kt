package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.util.SingleLiveEvent

class SignUpViewModel(application: Application) : AndroidViewModel(application) {
    private var user = User(0L, "", "")

    private val _signUpErrorState = MutableLiveData(AuthModel())
    val signUpErrorState: LiveData<AuthModel>
        get() = _signUpErrorState

    private val _signedUp = SingleLiveEvent<Unit>()
    val signedUp: LiveData<Unit>
        get() = _signedUp

    private var code: Int = 0

    fun clearErrorText() {
        _signUpErrorState.value = AuthModel()
    }

    private suspend fun register(login: String, password: String, name: String) {
        try {
            _signUpErrorState.value = AuthModel(signingInUp = true)
            code = 0
            val response = PostsApi.retrofitService.register(login, password, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            user = user.copy(id = body.id, token = body.token)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    fun signUp(login: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                register(login, password, name)
                AppAuth.getInstance().setAuth(user.id, user.token)
                _signUpErrorState.value = AuthModel()
                _signedUp.value = Unit
            } catch (e: Exception) {
                _signUpErrorState.value = AuthModel(unableSingIn = true)

            }
        }
    }
}


