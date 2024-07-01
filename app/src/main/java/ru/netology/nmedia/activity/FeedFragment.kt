package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.EditPostFragment.Companion.contentArg
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.activity.PhotoFragment.Companion.uriArg
import ru.netology.nmedia.activity.PostFragment.Companion.idArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)

        // диалоговое окно для аутентификации при like или создании поста
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        dialogBuilder
            .setTitle(getString(R.string.needToSignIn))
            .setNegativeButton(getString(R.string.back)) { dialog, _ ->
                dialog.cancel()

            }
            .setPositiveButton(getString(R.string.sign_in)) { _, _ ->
                findNavController().navigate(R.id.signInFragmentForNav)

            }

        val dialog: AlertDialog = dialogBuilder.create()


        val adapter = PostsAdapter(object : OnInteractionListener {
            // функция редактирования
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                // переход между фрагментами с передачей текста поста с ключом contentArg
                findNavController().navigate(
                    R.id.action_feedFragment_to_EditPostFragment,
                    Bundle().apply {
                        contentArg = post.content
                    })
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onLike(post: Post) {
                if (authViewModel.authenticated) {
                    viewModel.likeById(post)
                } else {
                    dialog.show()
                }
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(intent, null)
                startActivity(shareIntent)
                //viewModel.share(post.id)
            }


            // переход на фрагмент поста по клику на пост (кроме работающих кнопок) с передачей id поста через ключ idArg
            override fun onRoot(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_postFragment,
                    Bundle().apply {
                        idArg = post.id
                    })
            }

            override fun onPhoto(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_PhotoFragment,
                    Bundle().apply {
                        uriArg = post.attachment?.url
                    })
            }

        }
        )

        binding.list.adapter = adapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.data.collectLatest {
                    adapter.submitData(it)
                }
            }
        }

//        viewModel.data.observe(viewLifecycleOwner) { state ->
//            val newPost = state.posts.size > adapter.currentList.size && adapter.itemCount > 0
//            adapter.submitList(state.posts) {
//                if (newPost) {
//                    binding.list.smoothScrollToPosition(0)
//                }
//            }
//            binding.emptyText.isVisible = state.empty
//        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing

            if (state.isSaved) {
                binding.list.smoothScrollToPosition(0)
            }
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.refresh()
                    }
                    .show()
            }
            if (state.errorOfSave) {
                Snackbar.make(binding.root, R.string.error_save, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadUnsavedPosts()
                    }
                    .show()
            }
            if (state.errorOfDelete) {
                Snackbar.make(binding.root, R.string.error_delete, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry_loading) {
                        viewModel.removeById(state.id)
                    }
                    .show()
            }
            if (state.errorOfLike) {
                Snackbar.make(binding.root, R.string.error_likes, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry_loading) {
                        state.post?.let { post -> viewModel.likeById(post) }
                    }
                    .show()
            }
        }

        // меню в Top App Bar с количеством новых постов
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.options_on_main_menu, menu)
//                val newPostItemCount = menu.findItem(R.id.new_posts_number)

//                viewModel.newerCount.observe(viewLifecycleOwner) { count ->
//                    if (count > 0) {
//                        newPostItemCount.title = count.toString()
//                        newPostItemCount.isVisible = true
//                    } else {
//                        newPostItemCount.isVisible = false
//                    }
//                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
        }, viewLifecycleOwner)

//        viewModel.newerCount.observe(viewLifecycleOwner) { count ->
//            if (count > 0) {
//                binding.newPosts.text =
//                    String.format(getString(R.string.new_posts) + " (" + count + ")")
//                binding.newPosts.isVisible = true
//            } else {
//                binding.newPosts.isGone = true
//            }
//        }


        binding.newPosts.setOnClickListener {
            viewModel.updateNewPost()
            binding.list.smoothScrollToPosition(0)
            binding.newPosts.isGone = true
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                adapter.loadStateFlow.collectLatest {
                    binding.swiperefresh.isRefreshing = it.refresh is LoadState.Loading
                            || it.prepend is LoadState.Loading
                            || it.append is LoadState.Loading
                }
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
            //viewModel.loadUnsavedPosts()
        }

        // переход на фрагмент создания поста по клику кнопки +
        binding.newPostButton.setOnClickListener {
            if (authViewModel.authenticated) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = viewModel.getDraft()
                    })
            } else {
                dialog.show()
            }

        }



        return binding.root
    }
}