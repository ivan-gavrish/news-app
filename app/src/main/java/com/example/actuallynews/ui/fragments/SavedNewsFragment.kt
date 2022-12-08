package com.example.actuallynews.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actuallynews.R
import com.example.actuallynews.adapters.SavedNewsAdapter
import com.example.actuallynews.databinding.FragmentSavedNewsBinding
import com.example.actuallynews.models.DatabaseEntry
import com.example.actuallynews.shared.FirebaseCollections
import com.example.actuallynews.ui.ActuallyNewsActivity
import com.example.actuallynews.ui.ActuallyNewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedNewsFragment : Fragment() {
    private lateinit var binding: FragmentSavedNewsBinding
    private lateinit var viewModel: ActuallyNewsViewModel
    private lateinit var recyclerViewAdapter: SavedNewsAdapter
    private lateinit var collection: FirebaseCollections
    private val args: SavedNewsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSavedNewsBinding.inflate(inflater, container, false)
        collection = args.collection
        viewModel = (activity as ActuallyNewsActivity).viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        if (collection == FirebaseCollections.FAVORITES) {
            viewModel.favoriteArticles.observe(viewLifecycleOwner) { savedArticles ->
                recyclerViewAdapter.listDiffer.submitList(savedArticles)
            }
            binding.tvSavedNewsHeader.text = getString(R.string.favorites)
        } else {
            viewModel.bookmarkedArticles.observe(viewLifecycleOwner) { savedArticles ->
                recyclerViewAdapter.listDiffer.submitList(savedArticles)
            }
            binding.tvSavedNewsHeader.text = getString(R.string.bookmarks)
        }
        getSavedArticles(collection)
        setUpItemTouchHelper()
    }

    private fun getSavedArticles(collection: FirebaseCollections) {
        viewModel.getSavedArticles(viewModel.userEmail!!, collection)
    }

    private fun setUpRecyclerView() {
        recyclerViewAdapter = SavedNewsAdapter()
        binding.rvSavedNews.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(context)
        }
        recyclerViewAdapter.onItemClickListener = { entry ->
            findNavController().navigate(
                SavedNewsFragmentDirections.actionSavedNewsFragmentToArticleFragment(entry)
            )
        }
    }

    /**
     * Set up actions for interaction with recycler view
     */
    private fun setUpItemTouchHelper() {
        ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val entry =
                        recyclerViewAdapter.listDiffer.currentList[viewHolder.adapterPosition]
                    viewModel.deleteArticle(viewModel.userEmail!!, entry.articleUrl, collection)
                    Snackbar.make(
                        binding.root,
                        "Article removed from ${collection.name}",
                        Snackbar.LENGTH_LONG
                    ).apply {
                        setAction("Undo") {
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.saveArticle(
                                    DatabaseEntry(
                                        userEmail = viewModel.userEmail ?: "",
                                        articleTitle = entry.articleTitle ?: "",
                                        articleUrl = entry.articleUrl,
                                        articleImageUrl = entry.articleImageUrl ?: "",
                                        publicationDate = entry.publicationDate ?: ""
                                    ), collection
                                )
                            }
                        }
                        show()
                    }
                }
            }
        ).apply {
            attachToRecyclerView(binding.rvSavedNews)
        }
    }
}
