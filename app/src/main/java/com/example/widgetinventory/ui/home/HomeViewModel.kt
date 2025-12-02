package com.example.widgetinventory.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository = ProductRepository()
    val allProducts: Flow<List<Product>>

    init {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        allProducts = if (userId != null) {
            repository.getAllProducts(userId)
        } else {
            emptyFlow()
        }
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.delete(product.id)
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}
