package com.example.widgetinventory.data.repository

import com.example.widgetinventory.data.db.ProductDao
import com.example.widgetinventory.data.model.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    // Obtiene la lista de productos (para HU 3.0)
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    // Obtiene un producto por ID (para HU 5.0 y 6.0)
    fun getProductById(id: Int): Flow<Product> {
        return productDao.getProductById(id)
    }

    // Inserta un producto (para HU 4.0)
    suspend fun insert(product: Product) {
        productDao.insertProduct(product)
    }

    // Actualiza un producto (para HU 6.0)
    suspend fun update(product: Product) {
        productDao.updateProduct(product)
    }

    // Elimina un producto (para HU 5.0)
    suspend fun delete(product: Product) {
        productDao.deleteProduct(product)
    }

    // Obtiene los productos para el widget (HU 1.0)
    // Esta función es 'suspend' porque el Widget no usa Flow
    suspend fun getProductsForWidget(): List<Product> {
        return productDao.getAllProductsForWidget()
    }
}