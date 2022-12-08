package com.example.actuallynews.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.actuallynews.R
import com.example.actuallynews.databinding.FragmentUserProfileBinding
import com.example.actuallynews.shared.FirebaseCollections
import com.example.actuallynews.shared.Constants.Companion.SHARED_PREFERENCES_IS_GUEST
import com.example.actuallynews.shared.Constants.Companion.SHARED_PREFERENCES_NAME
import com.example.actuallynews.ui.ActuallyNewsActivity
import com.example.actuallynews.ui.ActuallyNewsViewModel
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth

class UserProfileFragment : Fragment() {
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var viewModel: ActuallyNewsViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        viewModel = (activity as ActuallyNewsActivity).viewModel
        sharedPreferences =
            requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isUserGuest()) {
            showWarning()
        } else {
            showUserProfileLayout()
        }
        setUpUserInfo()
        readFavoriteArticles()
        readBookmarkedArticles()
        signOut()
    }

    private fun isUserGuest() = FirebaseAuth.getInstance().currentUser == null

    private fun showWarning() {
        binding.apply {
            tvWarning.visibility = View.VISIBLE
            ivNotify.visibility = View.VISIBLE
            tvGoToSignInScreen.visibility = View.VISIBLE
            signInUserProfileLayout.visibility = View.INVISIBLE
        }

        binding.tvGoToSignInScreen.setOnClickListener {
            sharedPreferences.edit().putBoolean(SHARED_PREFERENCES_IS_GUEST, false).apply()
            findNavController().navigate(R.id.action_userProfileFragment_to_signInFragment)
        }
    }

    private fun showUserProfileLayout() {
        binding.apply {
            tvWarning.visibility = View.INVISIBLE
            ivNotify.visibility = View.INVISIBLE
            tvGoToSignInScreen.visibility = View.INVISIBLE
            signInUserProfileLayout.visibility = View.VISIBLE
        }
    }

    private fun setUpUserInfo() {
        Glide.with(requireContext()).load(viewModel.userProfilePictureUrl)
            .into(binding.ivUserProfilePicture)
        binding.tvUserName.text = viewModel.userName
        binding.tvUserEmail.text = viewModel.userEmail
    }

    private fun readFavoriteArticles() {
        binding.tvFavorites.setOnClickListener {
            findNavController().navigate(
                UserProfileFragmentDirections.actionUserProfileFragmentToSavedNewsFragment(
                    FirebaseCollections.FAVORITES
                )
            )
        }
    }

    private fun readBookmarkedArticles() {
        binding.tvBookmarks.setOnClickListener {
            findNavController().navigate(
                UserProfileFragmentDirections.actionUserProfileFragmentToSavedNewsFragment(
                    FirebaseCollections.BOOKMARKS
                )
            )
        }
    }

    private fun signOut() {
        binding.tvSingOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_userProfileFragment_to_signInFragment)
        }
    }
}