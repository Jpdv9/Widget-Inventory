package com.example.widgetinventory.ui.addproduct

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.widgetinventory.R
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.databinding.FragmentAddProductBinding
import com.google.android.material.textfield.TextInputEditText

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    // Asumimos que el View Binding se genera como FragmentAddProductBinding
    private val binding get() = _binding!!
    private val viewModel: AddProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextWatchers()

        // Criterio 1: Al dar clic en la flecha, lleva al usuario a la HU 3.0
        // Nota: En un Fragment, usar el `NavController` y `popBackStack()` es la forma estándar de "Atrás".
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Criterio 8: Guardar en la DB y volver al Home
        binding.btnGuardar.setOnClickListener {
            saveProduct()
        }
    }

    /**
     * Implementación de TextWatcher para habilitar/deshabilitar el botón Guardar
     * (Criterios 6 y 7).
     */
    private fun setupTextWatchers() {
        // Lista de EditTexts a monitorear
        val fields = listOf<TextInputEditText>(
            binding.etProductId,
            binding.etProductName,
            binding.etProductPrice,
            binding.etProductQuantity
        )

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // El botón se habilita SÓLO cuando todos los campos están llenos (Criterio 7)
                val allFieldsFilled = fields.all { it.text.toString().trim().isNotEmpty() }
                binding.btnGuardar.isEnabled = allFieldsFilled // Criterio 6 y 7
            }
        }

        // Asignar el TextWatcher a todos los campos
        fields.forEach { it.addTextChangedListener(textWatcher) }
    }


    /**
     * Recolecta, convierte y guarda el producto en la base de datos (Criterio 8).
     */
    private fun saveProduct() {
        // 1. Recolección y conversión (ya sabemos que no están vacíos gracias al TextWatcher)
        val id = binding.etProductId.text.toString().toIntOrNull()
        val name = binding.etProductName.text.toString().trim()
        val price = binding.etProductPrice.text.toString().toDoubleOrNull()
        val quantity = binding.etProductQuantity.text.toString().toIntOrNull()

        // Última validación de conversión (si fallan al ser convertidos a número/double)
        if (id == null || price == null || quantity == null) {
            Toast.makeText(requireContext(), "Error: Verifique que los campos ID, Precio y Cantidad sean números válidos.", Toast.LENGTH_LONG).show()
            return
        }

        // 2. Crear y guardar el producto (Criterio 8)
        val newProduct = Product(id = id, name = name, price = price, quantity = quantity)
        viewModel.insertProduct(newProduct)

        // 3. Volver al Home Inventario (HU 3.0) y mostrar éxito (Criterio 8)
        Toast.makeText(requireContext(), "Producto '$name' guardado correctamente.", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}