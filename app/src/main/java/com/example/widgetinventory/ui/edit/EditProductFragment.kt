package com.example.widgetinventory.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.widgetinventory.data.repository.ProductRepository
import com.example.widgetinventory.databinding.FragmentEditProductBinding

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EditViewModel

    // Obtiene los argumentos de navegación
    private val args: EditProductFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)

        // Configura la Factory y el ViewModel
        val repository = ProductRepository()
        val factory = EditViewModelFactory(repository, args.productId.toString())
        viewModel = ViewModelProvider(this, factory)[EditViewModel::class.java]

        // Conecta el ViewModel y el Lifecycle al DataBinding
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner

        // Observa la navegación
        viewModel.navigateBack.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                // Vuelve al fragmento anterior
                findNavController().popBackStack()
                viewModel.onNavigationDone()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}