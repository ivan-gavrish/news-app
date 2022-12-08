package com.example.actuallynews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.actuallynews.adapters.NewsAdapter
import com.example.actuallynews.databinding.FragmentBreakingNewsBinding
import com.example.actuallynews.models.DatabaseEntry
import com.example.actuallynews.shared.Resource
import com.example.actuallynews.ui.ActuallyNewsActivity
import com.example.actuallynews.ui.ActuallyNewsViewModel

class BreakingNewsFragment : Fragment() {
    private lateinit var binding: FragmentBreakingNewsBinding
    private lateinit var viewModel: ActuallyNewsViewModel
    private lateinit var recyclerViewAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBreakingNewsBinding.inflate(inflater, container, false)
        viewModel = (activity as ActuallyNewsActivity).viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        observeBreakingNews()
        viewModel.getBreakingNews()
    }

    private fun observeBreakingNews() {
        viewModel.breakingNews.observe(viewLifecycleOwner) { newsResponse ->
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
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun setUpRecyclerView() {
        recyclerViewAdapter = NewsAdapter()
        binding.rvBreakingNews.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(context)
        }
        recyclerViewAdapter.onItemClickListener = { article ->
            findNavController().navigate(
                BreakingNewsFragmentDirections.actionBreakingNewsFragmentToArticleFragment(
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
}