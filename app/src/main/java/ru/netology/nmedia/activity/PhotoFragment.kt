package ru.netology.nmedia.activity


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.netology.nmedia.databinding.FragmentPhotoBinding
import ru.netology.nmedia.handler.loadAttachment
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel


class PhotoFragment : Fragment() {
    companion object {
        var Bundle.uriArg: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPhotoBinding.inflate(layoutInflater, container, false)
        val BASE_URL = "http://10.0.2.2:9999//"

        val uri = requireArguments().uriArg
        binding.attachmentImageOnFragment.loadAttachment("${BASE_URL}media/${uri}")

        return binding.root
    }
}