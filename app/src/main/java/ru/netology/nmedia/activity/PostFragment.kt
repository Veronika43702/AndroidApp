package ru.netology.nmedia.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Number
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.viewmodel.PostViewModel


class PostFragment : Fragment() {
    companion object {
        var Bundle.idArg: Long? by LongArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(layoutInflater, container, false)
        val viewModel by activityViewModels<PostViewModel>()

        val editPostContract = registerForActivityResult(EditPostActivityContract()) { result ->
            result?.let { viewModel.changeContentAndSave(result) } ?: viewModel.cancelEdit()
        }

        val id = requireArguments().idArg
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post: Post? = posts.find { it.id == id }
            if (post == null) {
                findNavController().navigateUp()
                return@observe
            }
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                like.text = Number.setNumberView(post.likes)
                share.text = Number.setNumberView(post.share)
                viewsCount.text = Number.setNumberView(post.views)
                like.isChecked = post.likedByMe

                if (post.video.isNotEmpty()) {
                    binding.videoLayout.visibility = View.VISIBLE
                }

                like.setOnClickListener {
                    viewModel.likeById(post.id)
                }

                share.setOnClickListener {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(intent, null)
                    startActivity(shareIntent)
                    viewModel.share(post.id)
                }


                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_post)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    viewModel.removeById(post.id)
                                    true
                                }

                                R.id.edit -> {
                                    viewModel.edit(post)
                                    editPostContract.launch(post.content)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }

                videoButton.setOnClickListener {
                    val webpage: Uri = Uri.parse(post.video)
                    val intent = Intent(Intent.ACTION_VIEW, webpage)
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No suitable app found!", Toast.LENGTH_SHORT).show()
                    }
                }

                video.setOnClickListener {
                    val webpage: Uri = Uri.parse(post.video)
                    val intent = Intent(Intent.ACTION_VIEW, webpage)
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No suitable app found!", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
        return binding.root
    }
}