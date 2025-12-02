package com.example.widgetinventory.ui.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Obtenemos el productId desde el SavedStateHandle
    private val productId: String = savedStateHandle.get<String>("productId")!!

    private val productFlow = repository.getAllProducts().map { productList ->
        productList.find { it.id == productId }
    }
    val product: LiveData<Product?> = productFlow.asLiveData()

    val productIdText = product.map { p ->
        p?.let { "Id: ${it.id}" } ?: "Id: (Producto no encontrado)"
    }

    val editableName = MutableLiveData<String>()
    val editablePrice = MutableLiveData<String>()
    val editableQuantity = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            val productToEdit = productFlow.first()
            productToEdit?.let {
                editableName.postValue(it.name)
                editablePrice.postValue(it.price.toString())
                editableQuantity.postValue(it.quantity.toString())
            }
        }
    }

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

    private val _navigateBack = MutableLiveData<Boolean?>()
    val navigateBack: LiveData<Boolean?> = _navigateBack

    fun onBackClicked() {
        _navigateBack.value = true
    }

    fun onNavigationDone() {
        _navigateBack.value = null
    }

    fun onEditClick() {
        if (!areFieldsValid()) return
        val currentProduct = product.value ?: return

        try {
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
            // Manejar error
        }
    }
}