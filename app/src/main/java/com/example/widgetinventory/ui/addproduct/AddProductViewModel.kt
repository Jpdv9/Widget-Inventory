package com.example.widgetinventory.ui.addproduct

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.db.InventoryDatabase
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import kotlinx.coroutines.launch

class AddProductViewModel (application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository

    init {
        // Inicialización del repositorio (Arquitectura MVVM)
        val dao = InventoryDatabase.getDatabase(application).productDao()
        repository = ProductRepository(dao)
    }

    /**
     * Inserta el producto en la base de datos (Criterio 8).
     */
    fun insertProduct(product: Product) = viewModelScope.launch {
        repository.insert(product)
    }
}