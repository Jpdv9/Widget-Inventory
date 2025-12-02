package com.example.widgetinventory.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    val allProducts = repository.getAllProducts().asLiveData()

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.delete(product.id)
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}