package com.example.actuallynews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.actuallynews.R
import com.example.actuallynews.adapters.BreakingNewsAdapter
import com.example.actuallynews.databinding.FragmentHomeBinding
import com.example.actuallynews.models.Article
import com.example.actuallynews.models.DatabaseEntry
import com.example.actuallynews.shared.Resource
import com.example.actuallynews.ui.ActuallyNewsActivity
import com.example.actuallynews.ui.ActuallyNewsViewModel

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: ActuallyNewsViewModel
    private lateinit var recyclerViewAdapter: BreakingNewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = (activity as ActuallyNewsActivity).viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        viewModel.getHomeBreakingNews()
        readMoreBreakingNews()
        observeHomeBreakingNews()
    }

    private fun setUpRecyclerView() {
        recyclerViewAdapter = BreakingNewsAdapter()
        binding.rvBreakingNews.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
        }
        recyclerViewAdapter.onItemClickListener = { article ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToArticleFragment(
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

    /**
     * Set up the top (banner) news with the [article]
     */
    private fun setUpTopNews(article: Article) {
        binding.tvTopNewsTitle.text = article.title
        Glide.with(requireContext())
            .load(article.urlToImage)
            .error(R.drawable.global_news_logo)
            .into(binding.ivTopNewsImage)
        learnMore(article)
    }

    private fun learnMore(article: Article) {
        binding.tvLearnMore.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToArticleFragment(
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

    private fun readMoreBreakingNews() {
        binding.btnMore.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToBreakingNewsFragment()
            )
        }
    }

    /**
     * Observe [home breaking news][ActuallyNewsViewModel.homeBreakingNews] response and handle it's state
     */
    private fun observeHomeBreakingNews() {
        viewModel.homeBreakingNews.observe(viewLifecycleOwner) { newsResponse ->
            when (newsResponse) {
                is Resource.Success -> {
                    hideProgressBar()
                    newsResponse.data?.let {
                        // get the top news from the response (index 0)
                        setUpTopNews(it.articles[0])
                        recyclerViewAdapter.listDiffer.submitList(
                            it.articles.subList(1, it.articles.size).toList()
                        )
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    newsResponse.message?.let {
                        Toast.makeText(
                            activity,
                            "An error occurred: $it",
                            Toast.LENGTH_LONG
                        ).show()
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
}