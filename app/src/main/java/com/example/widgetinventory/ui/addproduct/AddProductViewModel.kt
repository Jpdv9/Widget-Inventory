package com.example.widgetinventory.ui.addproduct

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val auth: FirebaseAuth // Inyectamos FirebaseAuth
) : ViewModel() {

    // LiveData para los 4 campos de texto
    val code = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    val price = MutableLiveData<String>()
    val quantity = MutableLiveData<String>()

    // LiveData para habilitar/deshabilitar el botón
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

    // LiveData para la navegación
    private val _navigateToHome = MutableLiveData<Boolean?>()
    val navigateToHome: LiveData<Boolean?> = _navigateToHome

    fun onNavigationDone() {
        _navigateToHome.value = null
    }

    fun onBackClicked() {
        _navigateToHome.value = true
    }

    // Lógica de guardado
    fun onSaveClicked() {
        if (!areFieldsValid()) return

        try {
            // Usamos la instancia inyectada de auth
            val userId = auth.currentUser?.uid ?: return
            // Creamos el producto con los datos de los LiveData
            val newProduct = Product(
                id = "", // Se genera en la BD
                code = code.value!!.toInt(),
                name = name.value!!,
                price = price.value!!.toDouble(),
                quantity = quantity.value!!.toInt(),
                userId = userId
            )

            // Usamos una corrutina para insertar en la BD
            viewModelScope.launch {
                repository.insert(newProduct)
                _navigateToHome.value = true // Navega de regreso a Home
            }
        } catch (e: Exception) {
            // Manejar error (ej. si el usuario pone \"hola\" en el precio)
            // (Puedes mostrar un Toast aquí)
        }
    }
}