package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.SignInViewModel

class SignInFragment : Fragment() {
    private val viewModel: SignInViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentSignInBinding.inflate(layoutInflater, container, false)

        val login = binding.loginText.getText()
        val password = binding.passwordText.getText()
        binding.errorText.visibility = View.INVISIBLE

        binding.signInButton.setOnClickListener {
            if (!login.isNullOrEmpty() && !password.isNullOrEmpty()) {
                binding.errorText.visibility = View.INVISIBLE
                // отправка запроса на сервер (получение id и token) и сохранение user в appAuth
                viewModel.signIn(login.toString(), password.toString())
            } else {
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = getString(R.string.fieldsNotEmpty)
            }
        }

        viewModel.signInErrorState.observe(viewLifecycleOwner) { state ->
            binding.progressSignIn.isVisible = state.signingInUp

            if (state.wrongData) {
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.setText(R.string.worngLoginPass)
            }

            if (state.unableSingIn) {
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.setText(R.string.error_loading)
            }
        }

        viewModel.signedIn.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AndroidUtils.hideKeyboard(binding.login)
                AndroidUtils.hideKeyboard(binding.password)
                viewModel.clearErrorText()
                findNavController().navigateUp()
            }
        })

        return binding.root
    }
}