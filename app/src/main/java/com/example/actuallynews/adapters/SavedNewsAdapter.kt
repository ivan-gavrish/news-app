package com.example.actuallynews.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.actuallynews.databinding.ItemNewsPreviewBinding
import com.example.actuallynews.models.DatabaseEntry

class SavedNewsAdapter : RecyclerView.Adapter<SavedNewsAdapter.ArticleViewBinding>() {
    inner class ArticleViewBinding(val binding: ItemNewsPreviewBinding) :
            RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<DatabaseEntry>() {
        override fun areItemsTheSame(oldItem: DatabaseEntry, newItem: DatabaseEntry): Boolean {
            return oldItem.articleUrl == newItem.articleUrl
        }

        override fun areContentsTheSame(oldItem: DatabaseEntry, newItem: DatabaseEntry): Boolean {
            return oldItem == newItem
        }

    }
    val listDiffer = AsyncListDiffer(this, differCallback)
    var onItemClickListener: ((DatabaseEntry) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewBinding {
        return ArticleViewBinding(
            ItemNewsPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ArticleViewBinding, position: Int) {
        val entry = listDiffer.currentList[position]
        holder.binding.apply {
            Glide.with(root.context).load(entry.articleImageUrl).into(ivArticleImage)
            tvTitle.text = entry.articleTitle
            tvPublicationDate.text = entry.publicationDate.replace("T", " ")
                .substringBefore("Z")
        }
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(entry)
            }
        }
    }

    override fun getItemCount() = listDiffer.currentList.size
}