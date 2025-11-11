package com.example.widgetinventory.data.repository

import com.example.widgetinventory.data.db.ProductDao
import com.example.widgetinventory.data.model.Product
import kotlinx.coroutines.Dispatchers // <-- ¡AÑADE ESTE IMPORT!
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext // <-- ¡AÑADE ESTE IMPORT!

class ProductRepository(private val productDao: ProductDao) {

    // Obtiene la lista de productos
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    // Obtiene un producto por ID
    fun getProductById(id: Int): Flow<Product?> {
        return productDao.getProductById(id)
    }

    // Inserta un producto
    suspend fun insert(product: Product) {
        withContext(Dispatchers.IO) {
            productDao.insertProduct(product)
        }
    }

    // Actualiza un producto
    suspend fun update(product: Product) {
        withContext(Dispatchers.IO) {
            productDao.updateProduct(product)
        }
    }

    // Elimina un producto
    suspend fun delete(product: Product) {
        withContext(Dispatchers.IO) {
            productDao.deleteProduct(product)
        }
    }

    // Obtiene los productos para el widget
    suspend fun getProductsForWidget(): List<Product> {
        return withContext(Dispatchers.IO) {
            productDao.getAllProductsForWidget()
        }
    }
}