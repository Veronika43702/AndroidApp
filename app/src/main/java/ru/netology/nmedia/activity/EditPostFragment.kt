package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentEditPostBinding
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class EditPostFragment : Fragment() {
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
        binding.content.requestFocus()

        val content = requireArguments().contentArg
        binding.content.setText(content)
        binding.contentText.text = content
        binding.content.focusAndShowKeyboard()


        binding.save.setOnClickListener {
            if (!binding.content.text.isNullOrBlank()) {
                val newContent = binding.content.text.toString()
                viewModel.changeContentAndSave(newContent)
            }
            findNavController().navigateUp()
        }

        binding.closeEdit.setOnClickListener {
            viewModel.cancelEdit()
            findNavController().navigateUp()
        }
        return binding.root
    }
}