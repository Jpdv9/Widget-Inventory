package com.example.widgetinventory.ui.detail

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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class DetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DetailViewModel
    private lateinit var repository: ProductRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock de Firebase Auth (¡simplificado!)
        firebaseAuth = mock()
        firebaseUser = mock()
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("test_user_id")

        repository = mock()
        savedStateHandle = SavedStateHandle().apply {
            set("productId", "test_id_123")
        }

        val product = Product(id = "test_id_123", name = "Test Product", price = 15.0, quantity = 4)
        whenever(repository.getAllProducts("test_user_id")).thenReturn(flowOf(listOf(product)))

        // ¡El ViewModel ahora recibe el mock de auth en el constructor!
        viewModel = DetailViewModel(repository, firebaseAuth, savedStateHandle)

        // Observar LiveData
        viewModel.product.observeForever { }
        viewModel.total.observeForever { }
        viewModel.navigateTo.observeForever { }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        // Ya no se necesita clearAllCaches()
    }

    @Test
    fun `total debe ser calculado correctamente`() {
        // Dado
        // El producto se establece en el setup
        testDispatcher.scheduler.advanceUntilIdle() // ¡Solución! Esperar a que las corrutinas terminen.

        // Entonces
        val expectedTotal = 15.0 * 4
        assertThat(viewModel.total.value).isEqualTo(expectedTotal)
    }

    @Test
    fun `onDeleteProduct debe llamar a repository delete y navegar`() = runTest {
        // Dado
        // El producto se establece en el setup
        testDispatcher.scheduler.advanceUntilIdle() // Asegurarse de que el producto se cargue

        // Cuando
        viewModel.onDeleteProduct()
        testDispatcher.scheduler.advanceUntilIdle()

        // Entonces
        verify(repository).delete("test_id_123")
        assertThat(viewModel.navigateTo.value).isEqualTo(-1)
    }

    @Test
    fun `onEditClicked debe navegar para editar`() {
        // Cuando
        viewModel.onEditClicked()

        // Entonces
        assertThat(viewModel.navigateTo.value).isEqualTo(1)
    }

    @Test
    fun `onBackClicked debe navegar hacia atrás`() {
        // Cuando
        viewModel.onBackClicked()

        // Entonces
        assertThat(viewModel.navigateTo.value).isEqualTo(-1)
    }

    @Test
    fun `onNavigationDone debe resetear el estado de navegación`() {
        // Dado
        viewModel.onEditClicked() // Establecer un estado de navegación inicial

        // Cuando
        viewModel.onNavigationDone()

        // Entonces
        assertThat(viewModel.navigateTo.value).isNull()
    }
}