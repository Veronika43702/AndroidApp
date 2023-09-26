package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostsBinding
import ru.netology.nmedia.dto.Number

import ru.netology.nmedia.dto.Post

typealias Listener = (Post) -> Unit

class PostsAdapter(
    private val onLikeListener: Listener,
    private val OnShareListener: Listener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onLikeListener, OnShareListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostsBinding,
    private val onLikelistener: Listener,
    private val onShareListener: Listener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likeCount.text = Number.setNumberView(post.likes)
            shareCount.text = Number.setNumberView(post.share)
            viewsCount.text = Number.setNumberView(post.views)
            like.setImageResource(
                if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24
            )
            like.setOnClickListener {
                onLikelistener(post)
            }

            share.setOnClickListener {
                onShareListener(post)
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