package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.nmedia.databinding.ActivityEditPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard

class EditPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.content.requestFocus()

        val content = intent.getStringExtra(Intent.EXTRA_TEXT)
        binding.content.setText(content)
        binding.contentText.text = content
        binding.content.focusAndShowKeyboard()


        binding.save.setOnClickListener {
            val text = binding.content.text?.toString()
            if (text.isNullOrBlank()) {
                setResult(Activity.RESULT_CANCELED)
            } else {
                setResult(Activity.RESULT_OK, Intent().putExtra(Intent.EXTRA_TEXT, text))
            }
            finish()
        }

        binding.closeEdit.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

    }
}