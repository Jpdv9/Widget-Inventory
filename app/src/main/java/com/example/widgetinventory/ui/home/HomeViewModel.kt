package com.example.widgetinventory.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope

import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import kotlinx.coroutines.launch

class HomeViewModel (application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository
    val allProducts by lazy { repository.getAllProducts() }

    init {
        repository = ProductRepository()
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.delete(product)
    }
}