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
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.SignUpViewModel

class SignUpFragment : Fragment() {
    private val viewModel: SignUpViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)

        val name = binding.nameText.getText()
        val login = binding.loginText.getText()
        val password = binding.passwordText.getText()
        val confirmPassword = binding.confirmPasswordText.getText()
        binding.errorText.visibility = View.INVISIBLE

        binding.signUpButton.setOnClickListener {
            binding.errorText.visibility = View.INVISIBLE
            if (!name.isNullOrEmpty()  && !login.isNullOrEmpty() && !password.isNullOrEmpty() && !confirmPassword.isNullOrEmpty()) {
                if (password.toString() != confirmPassword.toString()) {
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = getString(R.string.passwordDiff)
                } else {
                    // отправка запроса на сервер (получение id и token) и сохранение user в appAuth
                    viewModel.signUp(
                        login.toString(),
                        password.toString(),
                        name.toString()
                    )
                }
            } else {
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = getString(R.string.fieldsNotEmpty)

            }
        }

        viewModel.signUpErrorState.observe(viewLifecycleOwner) { state ->
            if (state.unableSingIn) {
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = getString(R.string.error_loading)
            }

            binding.progressSignUp.isVisible = state.signingInUp
        }

        viewModel.signedUp.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
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