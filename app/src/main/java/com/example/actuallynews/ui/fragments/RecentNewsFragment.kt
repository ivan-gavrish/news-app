package com.example.actuallynews.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.actuallynews.adapters.NewsAdapter
import com.example.actuallynews.databinding.FragmentRecentNewsBinding
import com.example.actuallynews.models.DatabaseEntry
import com.example.actuallynews.shared.Resource
import com.example.actuallynews.ui.ActuallyNewsActivity
import com.example.actuallynews.ui.ActuallyNewsViewModel

class RecentNewsFragment : Fragment() {
    private lateinit var binding: FragmentRecentNewsBinding
    private lateinit var viewModel: ActuallyNewsViewModel
    private lateinit var recyclerViewAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecentNewsBinding.inflate(inflater, container, false)
        viewModel = (activity as ActuallyNewsActivity).viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        observeRecentNews()
        viewModel.getRecentNews()
    }

    private fun observeRecentNews() {
        viewModel.recentNews.observe(viewLifecycleOwner) { newsResponse ->
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
                        Log.d("RecentNewsFragment", it)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        recyclerViewAdapter = NewsAdapter()
        binding.rvRecentNews.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        recyclerViewAdapter.onItemClickListener = { article ->
            findNavController().navigate(
                RecentNewsFragmentDirections.actionRecentNewsFragmentToArticleFragment(
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

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }
}