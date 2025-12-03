package com.example.widgetinventory.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: ProductRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // 1. Creamos mocks simples.
        repository = mock()
        firebaseAuth = mock()
        firebaseUser = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        // 2. Instanciamos el ViewModel DIRECTAMENTE pasando los mocks.
        viewModel = HomeViewModel(repository, firebaseAuth)
    }

    @Test
    fun `init con usuario logueado debe obtener productos`() = runTest {
        // Dado
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("test_user_id")

        val products = listOf(Product(id = "1", name = "Test"))
        whenever(repository.getAllProducts("test_user_id")).thenReturn(flowOf(products))

        // Cuando
        createViewModel()

        // Entonces
        val result = viewModel.allProducts.first()
        assertThat(result).isEqualTo(products)
        verify(repository).getAllProducts("test_user_id")
    }

    @Test
    fun `init sin usuario logueado no debe obtener productos`() = runTest {
        // Dado
        whenever(firebaseAuth.currentUser).thenReturn(null)

        // Cuando
        createViewModel()

        // Entonces
        val result = viewModel.allProducts.first()
        assertThat(result).isEmpty()
    }

    @Test
    fun `deleteProduct debe llamar a repository delete`() = runTest {
        // Dado (Configuramos el usuario para que el init no falle)
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("test_user_id")
        whenever(repository.getAllProducts(any())).thenReturn(flowOf(emptyList()))

        createViewModel()
        val product = Product(id = "test_id")

        // Cuando
        viewModel.deleteProduct(product)
        testDispatcher.scheduler.advanceUntilIdle() // Esperamos a la corrutina

        // Entonces
        verify(repository).delete("test_id")
    }

    @Test
    fun `signOut debe llamar a FirebaseAuth signOut`() {
        // Dado
        whenever(firebaseAuth.currentUser).thenReturn(null)
        createViewModel()

        // Cuando
        viewModel.signOut()

        // Entonces
        verify(firebaseAuth).signOut()
    }
}