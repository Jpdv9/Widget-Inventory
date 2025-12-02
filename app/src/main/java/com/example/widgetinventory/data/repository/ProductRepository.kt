package com.example.widgetinventory.data.repository

import com.example.widgetinventory.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose

class ProductRepository {

    // Obtenemos la instancia de la base de datos de Firestore
    private val productsCollection = FirebaseFirestore.getInstance().collection("products")

    // Obtiene la lista de productos en tiempo real desde Firestore para un usuario específico
    fun getAllProducts(userId: String): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Cierra el Flow con error si algo sale mal
                    return@addSnapshotListener
                }

                val productList = snapshot?.documents?.mapNotNull { document ->
                    val product = document.toObject(Product::class.java)
                    product?.id = document.id // Asignamos el ID del documento
                    product
                } ?: emptyList()

                trySend(productList) // Enviamos la lista actualizada al Flow
            }

        // Cuando el Flow se cancele, removemos el listener para evitar memory leaks
        awaitClose { listener.remove() }
    }

    // Inserta un producto
    suspend fun insert(product: Product) {
        productsCollection.add(product).await()
    }

    // Actualiza un producto
    suspend fun update(product: Product) {
        if (product.id.isNotEmpty()) {
            productsCollection.document(product.id).set(product).await()
        }
    }

    // Elimina un producto por su ID
    suspend fun delete(productId: String) {
        productsCollection.document(productId).delete().await()
    }

    // Obtiene los productos para el widget (versión para Firestore) para un usuario específico
    suspend fun getProductsForWidget(userId: String): List<Product> {
        return try {
            val snapshot = productsCollection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.mapNotNull { document ->
                val product = document.toObject(Product::class.java)
                product?.id = document.id
                product
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}