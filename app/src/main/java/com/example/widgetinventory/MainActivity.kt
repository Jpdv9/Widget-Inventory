package com.example.widgetinventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.widgetinventory.data.db.InventoryDatabase
import com.example.widgetinventory.data.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        insertSampleData()
    }

    private fun insertSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = InventoryDatabase.getDatabase(this@MainActivity)
                val productDao = database.productDao()

                val currentProducts = productDao.getAllProductsForWidget()

                if (currentProducts.isEmpty()) {
                    val sampleProducts = listOf(
                        Product(id = 1, name = "Zapatos", price = 23000.0, quantity = 256),
                        Product(id = 3, name = "Teclado", price = 75000.0, quantity = 10),
                        Product(id = 12, name = "Mouse", price = 50000.0, quantity = 2),
                        Product(id = 45, name = "Monitor 24\"", price = 800000.0, quantity = 5)
                    )

                    sampleProducts.forEach { product ->
                        productDao.insertProduct(product)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}