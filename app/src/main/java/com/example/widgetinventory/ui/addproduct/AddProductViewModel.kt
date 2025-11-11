package com.example.widgetinventory.ui.addproduct

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import kotlinx.coroutines.launch

class AddProductViewModel(private val repository: ProductRepository) : ViewModel() {

    // 1. LiveData para los 4 campos de texto (para Two-Way DataBinding)
    val code = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    val price = MutableLiveData<String>()
    val quantity = MutableLiveData<String>()

    // 2. LiveData para habilitar/deshabilitar el botón (Criterio 6)
    val isFormValid = MediatorLiveData<Boolean>().apply {
        addSource(code) { value = areFieldsValid() }
        addSource(name) { value = areFieldsValid() }
        addSource(price) { value = areFieldsValid() }
        addSource(quantity) { value = areFieldsValid() }
    }

    private fun areFieldsValid(): Boolean {
        return !code.value.isNullOrBlank() &&
                !name.value.isNullOrBlank() &&
                !price.value.isNullOrBlank() &&
                !quantity.value.isNullOrBlank()
    }

    // 3. LiveData para la navegación
    private val _navigateToHome = MutableLiveData<Boolean?>()
    val navigateToHome: LiveData<Boolean?> = _navigateToHome

    fun onNavigationDone() {
        _navigateToHome.value = null
    }

    fun onBackClicked() {
        _navigateToHome.value = true
    }

    // 4. Lógica de guardado (Criterio 8)
    fun onSaveClicked() {
        if (!areFieldsValid()) return

        try {
            // Creamos el producto con los datos de los LiveData
            val newProduct = Product(
                id = code.value!!.toInt(),
                name = name.value!!,
                price = price.value!!.toDouble(),
                quantity = quantity.value!!.toInt()
            )

            // Usamos una corrutina para insertar en la BD
            viewModelScope.launch {
                repository.insert(newProduct)
                _navigateToHome.value = true // Navega de regreso a Home
            }
        } catch (e: Exception) {
            // Manejar error (ej. si el usuario pone "hola" en el precio)
            // (Puedes mostrar un Toast aquí)
        }
    }
}