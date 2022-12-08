package com.example.actuallynews.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.actuallynews.R
import com.example.actuallynews.databinding.FragmentSignInBinding
import com.example.actuallynews.shared.Constants.Companion.SHARED_PREFERENCES_IS_GUEST
import com.example.actuallynews.shared.Constants.Companion.SHARED_PREFERENCES_NAME
import com.example.actuallynews.ui.ActuallyNewsActivity
import com.example.actuallynews.ui.ActuallyNewsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private lateinit var viewModel: ActuallyNewsViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        viewModel = (activity as ActuallyNewsActivity).viewModel
        auth = FirebaseAuth.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (auth.currentUser != null) {
            saveUserCredentials()
            findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
        }

        if (sharedPreferences.getBoolean(SHARED_PREFERENCES_IS_GUEST, false)) {
            findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
        }

        signIn()
        continueAsGuest()
    }

    private fun continueAsGuest() {
        binding.tvContinueAsGuest.setOnClickListener {
            sharedPreferences.edit().putBoolean(SHARED_PREFERENCES_IS_GUEST, true).apply()
            findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
        }
    }

    private fun signIn() {
        binding.btnSignIn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.webclient_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            signInWithGoogle(googleSignInClient)
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(task)
        }
    }

    private fun signInWithGoogle(googleSignInClient: GoogleSignInClient) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account = task.result
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        saveUserCredentials()
                        sharedPreferences.edit().putBoolean(SHARED_PREFERENCES_IS_GUEST, false)
                            .apply()
                        findNavController().navigate(R.id.action_signInFragment_to_homeFragment)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            result.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), task.exception.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserCredentials() {
        viewModel.userName = auth.currentUser?.displayName
        viewModel.userEmail = auth.currentUser?.email
        viewModel.userProfilePictureUrl = auth.currentUser?.photoUrl.toString()
    }
}