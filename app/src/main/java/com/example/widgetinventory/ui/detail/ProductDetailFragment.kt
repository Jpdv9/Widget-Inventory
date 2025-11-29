package com.example.widgetinventory.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.widgetinventory.R
import com.example.widgetinventory.data.repository.ProductRepository
import com.example.widgetinventory.databinding.FragmentProductDetailBinding


class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DetailViewModel
    private val args: ProductDetailFragmentArgs by this.navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)

        // Configurar la Factory y el ViewModel
        val repository = ProductRepository()
        val factory = DetailViewModelFactory(repository, args.productId.toString())
        viewModel = ViewModelProvider(this, factory)[DetailViewModel::class.java]

        // Conectar el ViewModel y el Lifecycle al DataBinding
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner

        // Configurar los observadores de navegación
        setupNavigationObservers()

        // Configurar los clics
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
                        val action = ProductDetailFragmentDirections
                            .actionProductDetailFragmentToEditProductFragment(args.productId)
                        findNavController().navigate(action)
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