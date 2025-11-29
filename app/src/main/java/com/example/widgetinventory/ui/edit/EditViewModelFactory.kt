package com.example.widgetinventory.ui.edit


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.widgetinventory.data.repository.ProductRepository

class EditViewModelFactory(
    private val repository: ProductRepository,
    private val productId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditViewModel::class.java)) {
            return EditViewModel(repository, productId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}