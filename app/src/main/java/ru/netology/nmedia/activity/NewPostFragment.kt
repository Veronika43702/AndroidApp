package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel by activityViewModels<PostViewModel>()
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false)
        // фокус (курсив) на поле текста
        binding.content.requestFocus()

        // отображение черновика/intent от поделиться из др. приложения в поле создания текста поста
        binding.content.setText(arguments?.textArg.orEmpty())

        binding.save.setOnClickListener {
            viewModel.configureNewPost(binding.content.text.toString())
            viewModel.savePost()
            viewModel.clearDraft()
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }
        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
        }

        // отмена сохранения поста с сохранением черновика через системную кнопку "назад"
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val content = binding.content.text.toString()
                    viewModel.cancelSave(content)
                    // скрытие клавиатуры
                    AndroidUtils.hideKeyboard(binding.content)
                    // переход назад
                    findNavController().navigateUp()
                }
            }
        )
        return binding.root
    }
}