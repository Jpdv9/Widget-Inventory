package com.example.widgetinventory.ui.addproduct

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.widgetinventory.data.repository.ProductRepository
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AddProductViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AddProductViewModel
    private lateinit var repository: ProductRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock de Firebase Auth - ¡Simplificado!
        firebaseAuth = mock()
        firebaseUser = mock()

        repository = mock()
        // ¡Pasamos el mock de FirebaseAuth al constructor!
        viewModel = AddProductViewModel(repository, firebaseAuth)

        // Observar LiveData
        viewModel.isFormValid.observeForever { }
        viewModel.navigateToHome.observeForever { }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        // Ya no se necesita Mockito.clearAllCaches()
    }

    @Test
    fun `isFormValid debe ser true cuando todos los campos son válidos`() {
        // Cuando
        viewModel.code.value = "123"
        viewModel.name.value = "Product Name"
        viewModel.price.value = "10.99"
        viewModel.quantity.value = "5"

        // Entonces
        assertThat(viewModel.isFormValid.value).isTrue()
    }

    @Test
    fun `isFormValid debe ser false si algún campo está vacío`() {
        // Cuando
        viewModel.code.value = "123"
        viewModel.name.value = ""
        viewModel.price.value = "10.99"
        viewModel.quantity.value = "5"

        // Entonces
        assertThat(viewModel.isFormValid.value).isFalse()
    }

    @Test
    fun `onSaveClicked con datos válidos debe guardar y navegar`() = runTest {
        // Dado
        whenever(firebaseAuth.currentUser).thenReturn(firebaseUser)
        whenever(firebaseUser.uid).thenReturn("test_user_id")
        viewModel.code.value = "123"
        viewModel.name.value = "Product Name"
        viewModel.price.value = "10.99"
        viewModel.quantity.value = "5"

        // Cuando
        viewModel.onSaveClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Entonces
        verify(repository).insert(any())
        assertThat(viewModel.navigateToHome.value).isTrue()
    }

    @Test
    fun `onSaveClicked con datos no válidos no debe guardar ni navegar`() = runTest {
        // Dado
        viewModel.name.value = "" // campo no válido

        // Cuando
        viewModel.onSaveClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Entonces
        verify(repository, never()).insert(any())
        assertThat(viewModel.navigateToHome.value).isNull()
    }

    @Test
    fun `onBackClicked debe navegar a Home`() {
        // Cuando
        viewModel.onBackClicked()

        // Entonces
        assertThat(viewModel.navigateToHome.value).isTrue()
    }

    @Test
    fun `onNavigationDone debe restablecer el estado de navegación`() {
        // Dado
        viewModel.onBackClicked()

        // Cuando
        viewModel.onNavigationDone()

        // Entonces
        assertThat(viewModel.navigateToHome.value).isNull()
    }
}