package com.example.widgetinventory.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.widgetinventory.R
import com.example.widgetinventory.databinding.FragmentHomeBinding
import com.example.widgetinventory.ui.home.adapter.ProductAdapter
import com.example.widgetinventory.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint // 1. Importar esto

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Hilt se encarga automáticamente de crear la instancia correcta aquí
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Configuración del adaptador
        val adapter = ProductAdapter(emptyList()) { product ->
            val action = HomeFragmentDirections.actionHomeFragmentToProductDetailFragment(product.id)
            findNavController().navigate(action)
        }

        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProducts.adapter = adapter

        // Observamos los datos
        viewModel.allProducts.asLiveData().observe(viewLifecycleOwner) { products ->
            // Lógica simple: Si está vacía o cargando, muestra progress (puedes refinar esto luego)
            binding.progressBar.visibility = if (products.isNullOrEmpty()) View.VISIBLE else View.GONE
            adapter.updateList(products)
        }

        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addProductFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lógica de cerrar sesión
        binding.toolbar.findViewById<ImageView>(R.id.btnCerrarSesion).setOnClickListener {
            viewModel.signOut()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}