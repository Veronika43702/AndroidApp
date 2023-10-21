package ru.netology.nmedia.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditPostFragment.Companion.contentArg
import ru.netology.nmedia.activity.PostFragment.Companion.idArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel


class FeedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)
        val viewModel: PostViewModel by activityViewModels()

        val adapter = PostsAdapter(object : OnInteractionListener {
            // функция редактирования
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                // переход между фрагментами с передачей текста поста с ключом contentArg
                findNavController().navigate(R.id.action_feedFragment_to_EditPostFragment, Bundle().apply{
                    contentArg = post.content
                })
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(intent, null)
                startActivity(shareIntent)
                viewModel.share(post.id)
            }

            // открытие ссылки в youtube по клику на кнопку и поле картинки (после commit Fragment фукнция не проверялась)
            override fun openVideo(post: Post) {
                val webpage: Uri = Uri.parse(post.video)
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No suitable app found!", Toast.LENGTH_SHORT).show()
                }
            }

            // переход на фрагмент поста по клику на пост (кроме работающих кнопок) с передачей id поста через ключ idArg
            override fun onRoot(post: Post) {
                findNavController().navigate(R.id.action_feedFragment_to_postFragment, Bundle().apply{
                    idArg = post.id
                })
            }

        }
        )


        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            // проверка создания нового поста (размер стал больше)
            val newPost = adapter.currentList.size < posts.size
            adapter.submitList(posts) {
                if (newPost) {
                    // при новом посте отбражение списка с 1-ого элемента сверху (автопрокрутка)
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

        // переход на фрагмент создания поста по клику кнопки +
        binding.newPostButton.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}