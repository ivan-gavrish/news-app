package com.example.actuallynews.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.actuallynews.repository.ActuallyNewsRepository

class ActuallyNewsViewModelProviderFactory(
    private val actuallyNewsRepository: ActuallyNewsRepository,
    private val app: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ActuallyNewsViewModel(actuallyNewsRepository, app) as T
    }
}