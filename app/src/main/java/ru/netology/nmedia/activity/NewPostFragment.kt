package ru.netology.nmedia.activity

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory

class NewPostFragment : Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val dependencyContainer = DependencyContainer.getInstance()

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = {
            ViewModelFactory(
                dependencyContainer.repository,
                dependencyContainer.appAuth,
                dependencyContainer.apiService
            )
        }
    )

    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = {
            ViewModelFactory(
                dependencyContainer.repository,
                dependencyContainer.appAuth,
                dependencyContainer.apiService
            )
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        // фокус (курсив) на поле текста
        binding.content.requestFocus()

        // отображение черновика/intent от поделиться из др. приложения в поле создания текста поста
        binding.content.setText(arguments?.textArg.orEmpty())

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                        return@registerForActivityResult
                    }

                    Activity.RESULT_OK -> {
                        val uri = it.data?.data ?: return@registerForActivityResult
                        viewModel.savePhoto(PhotoModel(uri, uri.toFile()))
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.clearPhoto()
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }

            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }


        // диалоговое окно для аутентификации при like или создании поста
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        dialogBuilder
            .setTitle(getString(R.string.areYouSure))
            .setNegativeButton(getString(R.string.back)) { dialog, _ ->
                dialog.cancel()

            }
            .setPositiveButton(getString(R.string.sign_out)) { dialog, _ ->
                dependencyContainer.appAuth.removeAuth()
                findNavController().navigateUp()

            }

        val dialog: AlertDialog = dialogBuilder.create()

        // меню в Top App Bar с кнопкой save
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                menu.setGroupVisible(R.id.signoutFromNewPost, authViewModel.authenticated)
                menu.setGroupVisible(R.id.unauthenticated, !authViewModel.authenticated)
                menu.removeItem(R.id.signout)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        viewModel.configureNewPost(binding.content.text.toString())
                        viewModel.save()
                        viewModel.clearPhoto()
                        viewModel.clearDraft()
                        AndroidUtils.hideKeyboard(requireView())
                        true
                    }

                    R.id.signoutFromNewPost -> {
                        dialog.show()
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)

        viewModel.postCreated.observe(viewLifecycleOwner) {
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