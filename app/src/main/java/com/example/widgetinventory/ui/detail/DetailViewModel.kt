package com.example.widgetinventory.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: ProductRepository,
    private val productId: Int
) : ViewModel() {

    // Carga el producto desde la BD usando el ID
    val product: LiveData<Product?> = repository.getProductById(productId).asLiveData()

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
                repository.delete(it)
                // Navega de regreso al Home después de borrar el producto
                _navigateTo.value = -1
            }
        }
    }

    //Lógica para ir a Editar
    fun onEditClicked() {
        // Navega a la pantalla de Edición
        _navigateTo.value = 1
    }

    // Lógica para el botón de 'atrás'
    fun onBackClicked() {
        _navigateTo.value = -1
    }

    //Resetea el evento de navegación
    fun onNavigationDone() {
        _navigateTo.value = null
    }
}