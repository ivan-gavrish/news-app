package com.example.actuallynews.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.actuallynews.R
import com.example.actuallynews.databinding.ItemNewsPreviewBinding
import com.example.actuallynews.models.Article
import java.io.Serializable

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.ArticleViewBinding>() {
    inner class ArticleViewBinding(val binding: ItemNewsPreviewBinding) :
            RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
    val listDiffer = AsyncListDiffer(this, differCallback)
    var onItemClickListener: ((Article) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewBinding {
        return ArticleViewBinding(
            ItemNewsPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ArticleViewBinding, position: Int) {
        val article = listDiffer.currentList[position]
        holder.binding.apply {
            // handle null imageUrl
            Glide.with(root.context).load(article.urlToImage).error(R.drawable.news)
                .into(ivArticleImage)
            tvTitle.text = article.title
            tvPublicationDate.text = article.publishedAt?.replace("T", " ")
                ?.substringBefore("Z")
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(article)
            }
        }
    }

    override fun getItemCount() = listDiffer.currentList.size
}