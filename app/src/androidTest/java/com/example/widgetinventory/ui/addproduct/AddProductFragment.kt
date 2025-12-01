package com.example.widgetinventory.ui.addproduct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.widgetinventory.R
import com.example.widgetinventory.data.repository.ProductRepository
import com.example.widgetinventory.databinding.FragmentAddProductBinding

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)

        // Configurar la Factory y el ViewModel
        val repository = ProductRepository()
        val factory = AddProductViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AddProductViewModel::class.java]

        // Conectar el ViewModel y el Lifecycle al DataBinding
        binding.viewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner

        // 3. Observar la navegación
        viewModel.navigateToHome.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                // Volvemos al HomeFragment
                findNavController().navigate(R.id.action_addProductFragment_to_homeFragment)
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