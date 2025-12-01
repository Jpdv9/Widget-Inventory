package com.example.widgetinventory.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.widgetinventory.data.repository.ProductRepository

class DetailViewModelFactory(
    private val repository: ProductRepository,
    private val productId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(repository, productId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}