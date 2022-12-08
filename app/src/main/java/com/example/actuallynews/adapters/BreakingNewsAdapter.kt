package com.example.actuallynews.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.actuallynews.R
import com.example.actuallynews.databinding.ItemBreakingNewsArticlePreviewBinding
import com.example.actuallynews.models.Article
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BreakingNewsAdapter : RecyclerView.Adapter<BreakingNewsAdapter.ArticleViewBinding>() {
    inner class ArticleViewBinding(val binding: ItemBreakingNewsArticlePreviewBinding) :
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
            ItemBreakingNewsArticlePreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ArticleViewBinding, position: Int) {
        val article = listDiffer.currentList[position]
        holder.binding.apply {
            Glide.with(root.context).load(article.urlToImage).into(ivArticleImage)
            tvTitle.text = article.title
            tvAuthor.text = root.context.getString(
                R.string.author,
                article.author ?: "Unknown Author"
            )
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