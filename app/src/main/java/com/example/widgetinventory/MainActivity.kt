package com.example.widgetinventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Insertar datos de prueba automáticamente
        insertSampleData()
    }

    private fun insertSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = InventoryDatabase.getDatabase(this@MainActivity)
                val productDao = database.productDao()

                // Verificar si ya hay datos
                val currentBalance = productDao.getTotalBalance() ?: 0.0

                if (currentBalance == 0.0) {
                    // Insertar productos de ejemplo
                    val sampleProducts = listOf(
                        Product(code = "001", name = "Laptop Gamer", price = 2500000.0, quantity = 3),
                        Product(code = "002", name = "Mouse Inalámbrico", price = 45000.0, quantity = 15),
                        Product(code = "003", name = "Teclado Mecánico", price = 180000.0, quantity = 8),
                        Product(code = "004", name = "Monitor 24\"", price = 800000.0, quantity = 5)
                    )

                    sampleProducts.forEach { product ->
                        productDao.insertProduct(product)
                    }

                    println("✅ Datos de prueba insertados correctamente")
                } else {
                    println("✅ Ya existen datos en la BD")
                }
            } catch (e: Exception) {
                println("❌ Error insertando datos: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}