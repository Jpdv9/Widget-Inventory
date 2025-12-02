package com.example.widgetinventory.ui.edit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class EditViewModelTest {

    // Esta regla ejecuta todos los trabajos en segundo plano de los componentes de arquitectura en el mismo hilo
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: EditViewModel
    private lateinit var repository: ProductRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock de Firebase Auth
        firebaseAuth = mock()
        firebaseUser = mock()
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("test_user_id")

        repository = mock()
        savedStateHandle = SavedStateHandle().apply {
            set("productId", "test_id")
        }

        val product = Product(id = "test_id", code = 123, name = "Test Product", price = 10.0, quantity = 5)
        whenever(repository.getAllProducts(any())).thenReturn(flowOf(listOf(product)))

        // Pasamos el mock de FirebaseAuth al constructor
        viewModel = EditViewModel(repository, firebaseAuth, savedStateHandle)

        // Observar LiveData para activar las actualizaciones
        viewModel.product.observeForever { }
        viewModel.isFormValid.observeForever { }
        viewModel.navigateBack.observeForever { }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onBackClicked debe establecer navigateBack en true`() {
        // Cuando
        viewModel.onBackClicked()

        // Entonces
        assertThat(viewModel.navigateBack.value).isTrue()
    }

    @Test
    fun `onNavigationDone debe establecer navigateBack en null`() {
        // Dado
        viewModel.onBackClicked() // Establecer el estado inicial

        // Cuando
        viewModel.onNavigationDone()

        // Entonces
        assertThat(viewModel.navigateBack.value).isNull()
    }

    @Test
    fun `onEditClick con datos válidos debe actualizar el producto y navegar hacia atrás`() = runTest {
        // Dado: el bloque init del viewModel se ejecutará, llenando los campos editables.
        // Esperamos a que se complete el bloque init.
        testDispatcher.scheduler.advanceUntilIdle()

        // Ahora, actualizamos los campos con datos válidos.
        viewModel.editableName.value = "Updated Name"
        viewModel.editablePrice.value = "25.50"
        viewModel.editableQuantity.value = "50"

        // Cuando
        viewModel.onEditClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Entonces
        // Verificamos que se llamó al método de actualización del repositorio con el producto correcto
        val currentProduct = viewModel.product.value!!
        val expectedUpdatedProduct = currentProduct.copy(
             name = "Updated Name",
             price = 25.50,
             quantity = 50
        )

        verify(repository).update(expectedUpdatedProduct)

        // Y verificamos que se activa la navegación
        assertThat(viewModel.navigateBack.value).isTrue()
    }

    @Test
    fun `onEditClick con datos no válidos no debe actualizar ni navegar`() = runTest {
        // Dado
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.editableName.value = "" // Nombre no válido

        // Cuando
        viewModel.onEditClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Entonces
        verify(repository, never()).update(any())
        assertThat(viewModel.navigateBack.value).isNull()
    }
}