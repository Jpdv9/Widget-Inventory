package com.example.widgetinventory.ui.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class EditViewModel(
    private val repository: ProductRepository,
    private val productId: Int
) : ViewModel() {

    //Carga el producto original desde la base de datos
    private val _originalProduct = repository.getProductById(productId)

    //Variables para el ID del producto
    val productIdText = _originalProduct.map { product ->
        product?.let {
            "Id: ${it.id}"
        } ?: "Id: (Producto no encontrado)"
    }.asLiveData()

    //Campos editables para el nombre, precio y cantidad
    val editableName = MutableLiveData<String>()
    val editablePrice = MutableLiveData<String>()
    val editableQuantity = MutableLiveData<String>()

    // Carga los datos iniciales en los campos editables
    init {

        viewModelScope.launch {
            _originalProduct.collect { product ->
                if (product != null) {
                    editableName.postValue(product.name)
                    editablePrice.postValue(product.price.toString())
                    editableQuantity.postValue(product.quantity.toString())
                }
            }
        }
    }

    //Lógica de validación de campos
    val isFormValid = MediatorLiveData<Boolean>().apply {
        addSource(editableName) { value = areFieldsValid() }
        addSource(editablePrice) { value = areFieldsValid() }
        addSource(editableQuantity) { value = areFieldsValid() }
    }

    private fun areFieldsValid(): Boolean {
        return !editableName.value.isNullOrBlank() &&
                !editablePrice.value.isNullOrBlank() &&
                !editableQuantity.value.isNullOrBlank()
    }

    //Navegación
    private val _navigateBack = MutableLiveData<Boolean?>()
    val navigateBack: LiveData<Boolean?> = _navigateBack

    fun onBackClicked() {
        _navigateBack.value = true
    }

    fun onNavigationDone() {
        _navigateBack.value = null
    }

    //Lógica para editar el producto
    fun onEditClick() {
        if (!areFieldsValid()) return

        // Creamos un nuevo objeto Product con los datos actualizados
        try {
            val updatedProduct = Product(
                id = productId,
                name = editableName.value!!,
                price = editablePrice.value!!.toDouble(),
                quantity = editableQuantity.value!!.toInt()
            )

            viewModelScope.launch {
                repository.update(updatedProduct)
                _navigateBack.value = true
            }
        } catch (e: NumberFormatException) {

        }
    }
}