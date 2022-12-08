package com.example.actuallynews.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.actuallynews.R
import com.example.actuallynews.api.RetrofitInstance
import com.example.actuallynews.databinding.ActivityActuallyNewsBinding
import com.example.actuallynews.repository.ActuallyNewsRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ActuallyNewsActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityActuallyNewsBinding
    lateinit var viewModel: ActuallyNewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActuallyNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                as NavHostFragment).navController
        binding.bottomNavigationView.setupWithNavController(navController)
        viewModel = ViewModelProvider(
            this,
            ActuallyNewsViewModelProviderFactory(
                ActuallyNewsRepository(
                    RetrofitInstance.api,
                    Firebase.firestore
                ), application
            )
        )[ActuallyNewsViewModel::class.java]

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (
                destination.id == R.id.signInFragment ||
                destination.id == R.id.articleFragment ||
                destination.id == R.id.savedNewsFragment ||
                destination.id == R.id.breakingNewsFragment
            ) {
                binding.bottomNavigationView.visibility = View.INVISIBLE
            } else {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }
}