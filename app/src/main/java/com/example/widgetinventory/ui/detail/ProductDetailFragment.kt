package com.example.widgetinventory.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.widgetinventory.databinding.FragmentProductDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)


        // Conectamos el ViewModel y el Lifecycle al DataBinding
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner

        // Configurar los observadores
        setupNavigationObservers()
        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {
        // Botón de Eliminar
        binding.btnDeleteProduct.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // FAB de Editar
        binding.fabEditProduct.setOnClickListener {
            viewModel.onEditClicked()
        }

        // Flecha de 'atrás' en la Toolbar
        binding.toolbarDetail.setNavigationOnClickListener {
            viewModel.onBackClicked()
        }
    }

    private fun setupNavigationObservers() {
        viewModel.navigateTo.observe(viewLifecycleOwner) { destination ->
            destination?.let {
                when (it) {
                    -1 -> {
                        findNavController().popBackStack()
                    }
                    1 -> {
                        // Navegación más robusta, usando el ID del producto desde el ViewModel
                        viewModel.product.value?.id?.let { prodId ->
                            val action = ProductDetailFragmentDirections
                                .actionProductDetailFragmentToEditProductFragment(prodId)
                            findNavController().navigate(action)
                        }
                    }
                }
                viewModel.onNavigationDone()
            }
        }
    }

    // Muestra el diálogo de confirmación
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar este producto?")
            .setPositiveButton("Si") { _, _ ->
                viewModel.onDeleteProduct()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
