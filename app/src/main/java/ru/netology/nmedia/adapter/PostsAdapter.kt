package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostsBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Number
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.handler.loadAttachment
import ru.netology.nmedia.handler.loadAvatars

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onRoot(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostsBinding,
    private val onInteractionListener: OnInteractionListener

) : RecyclerView.ViewHolder(binding.root) {
    val BASE_URL = "http://10.0.2.2:9999//"
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published.toString() //PublishedDateTime.getTime(post.published)
            content.text = post.content
            like.text = Number.setNumberView(post.likes)
            share.text = Number.setNumberView(post.share)
            viewsCount.text = Number.setNumberView(post.views)
            like.isChecked = post.likedByMe
            avatar.loadAvatars("${BASE_URL}avatars/${post.authorAvatar}")

            if (post.attachment != null){
                when (post.attachment!!.type){
                    AttachmentType.IMAGE -> {
                        attachmentImage.visibility = View.VISIBLE
                        line.visibility = View.VISIBLE
                        attachmentImage.loadAttachment("${BASE_URL}images/${post.attachment!!.url}")
                        attachmentImage.contentDescription = post.attachment!!.description
                    }}
            } else {
                attachmentImage.visibility = View.GONE
                line.visibility = View.GONE
            }

            if (post.id > 0) {
                like.isClickable = true
                share.isClickable = true
                waitLoad.visibility = View.GONE

                like.setOnClickListener {
                    onInteractionListener.onLike(post)
                }

                share.setOnClickListener {
                    onInteractionListener.onShare(post)
                }

                root.setOnClickListener {
                    onInteractionListener.onRoot(post)
                }

                content.setOnClickListener {
                    onInteractionListener.onRoot(post)
                }
            } else {
                like.isClickable = false
                share.isClickable = false
                waitLoad.visibility = View.VISIBLE

            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }

    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }
}