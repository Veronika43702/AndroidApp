package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.databinding.ActivityNewPostBinding
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.content.requestFocus()
        binding.save.setOnClickListener {
            val text = binding.content.text?.toString()
            if (text.isNullOrBlank()) {
                setResult(Activity.RESULT_CANCELED)
            } else {
                setResult(Activity.RESULT_OK, Intent().putExtra(Intent.EXTRA_TEXT, text))
            }
            finish()
        }
    }
}