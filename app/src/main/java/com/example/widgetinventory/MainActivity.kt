package com.example.widgetinventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.widgetinventory.data.db.InventoryDatabase
import com.example.widgetinventory.data.model.Product
import com.example.widgetinventory.databinding.ActivityMainBinding // Importa tu binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Infla la vista usando DataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Insertar datos de prueba automáticamente
        insertSampleData()
    }

    private fun insertSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = InventoryDatabase.getDatabase(this@MainActivity)
                val productDao = database.productDao()

                // --- CORRECCIÓN 1: Comprobar si la lista está vacía ---
                // getAllProductsForWidget() devuelve una Lista.
                // Usamos .isEmpty() para saber si hay datos.
                val currentProducts = productDao.getAllProductsForWidget()

                if (currentProducts.isEmpty()) {
                    // --- CORRECCIÓN 2: Usar el modelo 'Product' correctamente ---
                    // El ID es un 'Int', no un 'String' llamado 'code'.
                    val sampleProducts = listOf(
                        Product(id = 1, name = "Zapatos", price = 23000.0, quantity = 256),
                        Product(id = 3, name = "Teclado", price = 75000.0, quantity = 10),
                        Product(id = 12, name = "Mouse", price = 50000.0, quantity = 2),
                        Product(id = 45, name = "Monitor 24\"", price = 800000.0, quantity = 5)
                    )

                    sampleProducts.forEach { product ->
                        productDao.insertProduct(product)
                    }

                    // (Opcional) Puedes imprimir en el Logcat para verificar
                    // Log.d("MainActivity", "✅ Datos de prueba insertados correctamente")

                } else {
                    // Log.d("MainActivity", "✅ Ya existen datos en la BD")
                }
            } catch (e: Exception) {
                // Log.e("MainActivity", "❌ Error insertando datos: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}