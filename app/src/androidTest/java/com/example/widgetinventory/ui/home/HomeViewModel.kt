package com.example.widgetinventory.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository = ProductRepository()
    val allProducts = repository.getAllProducts()

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.delete(product.id)
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}
