package com.example.actuallynews.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.actuallynews.adapters.NewsAdapter
import com.example.actuallynews.databinding.FragmentSearchNewsBinding
import com.example.actuallynews.models.DatabaseEntry
import com.example.actuallynews.shared.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.actuallynews.shared.Resource
import com.example.actuallynews.ui.ActuallyNewsActivity
import com.example.actuallynews.ui.ActuallyNewsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment() {
    private lateinit var binding: FragmentSearchNewsBinding
    private lateinit var viewModel: ActuallyNewsViewModel
    private lateinit var recyclerViewAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        viewModel = (activity as ActuallyNewsActivity).viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        observeSearchedNews()
        searchNews()
    }

    private fun setUpRecyclerView() {
        recyclerViewAdapter = NewsAdapter()
        binding.rvSearchNews.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(context)
        }
        recyclerViewAdapter.onItemClickListener = { article ->
            findNavController().navigate(
                SearchNewsFragmentDirections.actionSearchFragmentToArticleFragment(
                    DatabaseEntry(
                        userEmail = viewModel.userEmail!!,
                        articleTitle = article.title ?: "",
                        articleUrl = article.url,
                        articleImageUrl = article.urlToImage ?: "",
                        publicationDate = article.publishedAt ?: ""
                    )
                )
            )
        }
    }

    private fun observeSearchedNews() {
        viewModel.searchedNews.observe(viewLifecycleOwner) { newsResponse ->
            when (newsResponse) {
                is Resource.Success -> {
                    hideProgressBar()
                    newsResponse.data?.let { response ->
                        recyclerViewAdapter.listDiffer.submitList(response.articles.toList())
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    newsResponse.message?.let {
                        Toast.makeText(activity, "An error occurred: $it", Toast.LENGTH_LONG).show()
                        Log.d("SearchNewsFragment", it)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun searchNews() {
        var job: Job? = null
        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.searchNews(editable.toString())
                        hideSearchImage()
                    } else {
                        showSearchImage()
                    }
                }
            }
        }
    }

    private fun showSearchImage() {
        binding.apply {
            rvSearchNews.visibility = View.INVISIBLE
            ivSearchImage.visibility = View.VISIBLE
        }
    }

    private fun hideSearchImage() {
        binding.apply {
            rvSearchNews.visibility = View.VISIBLE
            ivSearchImage.visibility = View.INVISIBLE
        }
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }
}