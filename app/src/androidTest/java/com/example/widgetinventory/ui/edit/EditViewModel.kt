package com.example.widgetinventory.ui.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class EditViewModel(
    private val repository: ProductRepository,
    private val productId: String
) : ViewModel() {

    // Cargamos el producto desde el Flow de la lista
    // Este Flow busca el producto específico en la lista que viene de Firestore
    private val productFlow = repository.getAllProducts().map { productList ->
        productList.find { it.id == productId }
    }
    // Convertimos el Flow a LiveData para usarlo en la UI y en otras partes del ViewModel
    val product: LiveData<Product?> = productFlow.asLiveData()


    // Mantenemos el ID como un LiveData para la UI
    val productIdText = product.map { p ->
        p?.let { "Id: ${it.id}" } ?: "Id: (Producto no encontrado)"
    }

    // Campos editables
    val editableName = MutableLiveData<String>()
    val editablePrice = MutableLiveData<String>()
    val editableQuantity = MutableLiveData<String>()

    // Carga los datos iniciales en los campos editables
    init {
        viewModelScope.launch {
            // Recolectamos el Flow una sola vez para poblar los campos
            val productToEdit = productFlow.first() // .first() obtiene el primer valor emitido
            productToEdit?.let {
                editableName.postValue(it.name)
                editablePrice.postValue(it.price.toString())
                editableQuantity.postValue(it.quantity.toString())
            }
        }
    }

    // Lógica de validación
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

    // Navegación
    private val _navigateBack = MutableLiveData<Boolean?>()
    val navigateBack: LiveData<Boolean?> = _navigateBack

    fun onBackClicked() {
        _navigateBack.value = true
    }

    fun onNavigationDone() {
        _navigateBack.value = null
    }

    // Lógica para editar el producto
    fun onEditClick() {
        if (!areFieldsValid()) return

        // Obtenemos el producto actual para no perder datos como el 'code'
        val currentProduct = product.value ?: return

        try {
            // Creamos una *copia* del producto con los datos actualizados.
            // Así, el 'id' y el 'code' se mantienen intactos.
            val updatedProduct = currentProduct.copy(
                name = editableName.value!!,
                price = editablePrice.value!!.toDouble(),
                quantity = editableQuantity.value!!.toInt()
            )

            viewModelScope.launch {
                repository.update(updatedProduct)
                _navigateBack.value = true
            }
        } catch (e: NumberFormatException) {
            // Manejar error en caso de que el usuario ponga texto en campos numéricos
        }
    }
}