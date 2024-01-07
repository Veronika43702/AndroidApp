package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentEditPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class EditPostFragment : Fragment() {
    // строковые значения по ключу contentArg для получения данных из других фрагментов
    companion object {
        var Bundle.contentArg: String? by StringArg
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel by activityViewModels<PostViewModel>()
        val binding = FragmentEditPostBinding.inflate(layoutInflater, container, false)
        // фокус (курсив) на поле текста
        binding.content.requestFocus()

        // присвоение полученной строки от других фрагментов по ключу contentArg
        val content = requireArguments().contentArg
        // отображение полученной строки в поле редактирования поста
        binding.content.setText(content)
        // отображение полученной строки в заголовке окна редактирования
        binding.contentText.text = content
        // отображение клавиатуры
        binding.content.focusAndShowKeyboard()

        // обработка кнопки "сохранить/save"
        binding.save.setOnClickListener {
            if (!binding.content.text.isNullOrBlank()) {
                // сохранение текста в пост (в PostViewModel)
                viewModel.editPost(binding.content.text.toString())
            } else {
                // если текст пустой, то пост не сохраняем и очищаем edited
                viewModel.cancelEdit()
            }
            // навигация по фрагментам назад
            findNavController().navigateUp()
        }

        // обработка кнопки "отмена редакатирования/cancel edit"
        binding.closeEdit.setOnClickListener {
            // отмена измененей (очистка edited в PostViewModel)
            viewModel.cancelEdit()
            findNavController().navigateUp()
        }

        // отмена изменений через системную кнопку "назад"
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // сообщение перед действием
                    AlertDialog.Builder(requireActivity()).apply {
                        // заголовок сообщений
                        setTitle(R.string.cancel_edit_title)
                        // текст сообщения
                        setMessage(R.string.cancel_edit_message)
                        // вариант ответа "да" = переход назад
                        setPositiveButton(R.string.choice_yes) { _, _ ->
                            // отмена изменения (очистка edited в PostViewModel)
                            viewModel.cancelEdit()
                            // скрытие клавиатуры
                            AndroidUtils.hideKeyboard(binding.contentText)
                            // переход назад
                            findNavController().navigateUp()
                        }
                        // вариант ответа "нет" = остаемся на текущей странице
                        setNegativeButton(R.string.choice_no) { _, _ -> }
                        setCancelable(true)
                    }.create().show()
                }
            }
        )

        return binding.root
    }
}