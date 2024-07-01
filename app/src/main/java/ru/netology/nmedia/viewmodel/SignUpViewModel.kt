package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel  @Inject constructor (
    private val appAuth: AppAuth,
    private val apiService: ApiService
) : ViewModel() {
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
            val response = apiService.register(login, password, name)
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
                appAuth.setAuth(user.id, user.token)
                _signUpErrorState.value = AuthModel()
                _signedUp.value = Unit
            } catch (e: Exception) {
                _signUpErrorState.value = AuthModel(unableSingIn = true)

            }
        }
    }
}


