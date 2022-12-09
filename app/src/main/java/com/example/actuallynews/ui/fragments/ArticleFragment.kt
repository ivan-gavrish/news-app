package com.example.actuallynews.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.actuallynews.databinding.FragmentArticleBinding
import com.example.actuallynews.models.DatabaseEntry
import com.example.actuallynews.shared.FirebaseCollections
import com.example.actuallynews.shared.Constants.Companion.SHARED_PREFERENCES_IS_GUEST
import com.example.actuallynews.shared.Constants.Companion.SHARED_PREFERENCES_NAME
import com.example.actuallynews.ui.ActuallyNewsActivity
import com.example.actuallynews.ui.ActuallyNewsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArticleFragment : Fragment() {
    private lateinit var binding: FragmentArticleBinding
    private lateinit var viewModel: ActuallyNewsViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private val args: ArticleFragmentArgs by navArgs()
    private var entry: DatabaseEntry? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArticleBinding.inflate(inflater, container, false)
        viewModel = (activity as ActuallyNewsActivity).viewModel
        entry = args.entry
        sharedPreferences =
            requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWebView(entry!!.articleUrl)

        if (sharedPreferences.getBoolean(SHARED_PREFERENCES_IS_GUEST, false)) {
            hideReactionButtons()
        } else {
            showReactionButtons()
        }

        likeArticle(entry!!)
        bookmarkArticle(entry!!)
        shareArticle(entry!!.articleUrl)
    }

    private fun setUpWebView(url: String) {
        binding.wbArticle.apply {
            webViewClient = WebViewClient()
            settings.loadsImagesAutomatically = true
            settings.javaScriptEnabled = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            WebView.setWebContentsDebuggingEnabled(false)
            loadUrl(url)
        }
    }

    private fun hideReactionButtons() {
        binding.fabLikeArticle.visibility = View.INVISIBLE
        binding.fabBookmarkArticle.visibility = View.INVISIBLE
    }

    private fun showReactionButtons() {
        binding.fabLikeArticle.visibility = View.VISIBLE
        binding.fabBookmarkArticle.visibility = View.VISIBLE
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun likeArticle(entry: DatabaseEntry) {
        binding.fabLikeArticle.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (viewModel.saveArticle(entry, FirebaseCollections.FAVORITES)) {
                        withContext(Dispatchers.Main) {
                            showToastMessage("Article is saved to Favorites")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showToastMessage(e.message.toString())
                    }
                }
            }
        }
    }

    private fun bookmarkArticle(entry: DatabaseEntry) {
        binding.fabBookmarkArticle.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (viewModel.saveArticle(entry, FirebaseCollections.BOOKMARKS)) {
                        withContext(Dispatchers.Main) {
                            showToastMessage("Article is saved to Bookmarks")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showToastMessage(e.message.toString())
                    }
                }
            }
        }
    }

    private fun shareArticle(articleUrl: String) {
        binding.fabShareArticle.setOnClickListener {
            startActivity(
                Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, articleUrl)
                    type = "text/plain"
                }, "Share via ")
            )
        }
    }
}