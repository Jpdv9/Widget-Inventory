package com.example.widgetinventory.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.widgetinventory.databinding.FragmentHomeBinding
import com.example.widgetinventory.ui.home.adapter.ProductAdapter
import android.content.Context
import android.content.Intent
import com.example.widgetinventory.LoginActivity
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.example.widgetinventory.R

const val PREF_NAME = "InventoryPrefs"
const val PREF_SESSION_KEY = "is_logged_in"


class HomeFragment  : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val adapter = ProductAdapter(emptyList()) { product ->
            // Aquí más adelante abrirás el detalle (HU5)
        }

        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProducts.adapter = adapter

        // Mostrar lista en tiempo real
        viewModel.allProducts.observe(viewLifecycleOwner) { products ->
            binding.progressBar.visibility = View.GONE
            adapter.updateList(products)
        }

        // Botón flotante → HU4 (Agregar producto)
        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(R.id.addProductFragment)
        }

        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.toolbar.findViewById<ImageView>(R.id.btnCerrarSesion).setOnClickListener {

            // 1. Borrar la sesión guardada
            clearSession()


            val intent = Intent(requireContext(), LoginActivity::class.java)

            //LIMPIAR LA PILA DE ACTIVIDADES
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun clearSession() {

        val sharedPref = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        with (sharedPref.edit()) {
            remove(PREF_SESSION_KEY) // Elimina la clave de sesión
            apply()
        }
    }


}