package com.example.widgetinventory.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    savedStateHandle: SavedStateHandle //
) : ViewModel() {

    // Obtenemos el productId desde el SavedStateHandle
    private val productId: String = savedStateHandle.get<String>("productId")!!

    //

    // Carga el producto desde el Flow de la lista completa
    val product: LiveData<Product?> = repository.getAllProducts().map { productList ->
        productList.find { it.id == productId }
    }.asLiveData()

    // Calcula el total del producto
    val total: LiveData<Double> = product.map { product ->
        product?.let {
            it.price * it.quantity
        } ?: 0.0
    }

    //  Eventos de navegación
    private val _navigateTo = MutableLiveData<Int?>()
    val navigateTo: LiveData<Int?> = _navigateTo

    // Lógica para eliminar el producto
    fun onDeleteProduct() {
        viewModelScope.launch {
            product.value?.let {
                // Pasamos solo el ID al repositorio para eliminar el producto
                repository.delete(it.id)
                _navigateTo.value = -1
            }
        }
    }

    // Lógica para editar el producto
    fun onEditClicked() {
        _navigateTo.value = 1
    }

    fun onBackClicked() {
        _navigateTo.value = -1
    }

    fun onNavigationDone() {
        _navigateTo.value = null
    }
}