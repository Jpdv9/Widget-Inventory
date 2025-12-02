package com.example.widgetinventory.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val allProducts: Flow<List<Product>>

    init {
        val userId = auth.currentUser?.uid
        allProducts = if (userId != null) {
            repository.getAllProducts(userId)
        } else {
            flowOf(emptyList())
        }
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.delete(product.id)
    }

    fun signOut() {
        auth.signOut()
    }
}