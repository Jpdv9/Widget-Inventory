package com.example.widgetinventory.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.widgetinventory.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // Inserta un producto
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: Product)

    // Actualiza un producto
    @Update
    fun updateProduct(product: Product)

    // Elimina un producto
    @Delete
    fun deleteProduct(product: Product)

    // Usamos Flow para que la UI se actualice automáticamente
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    // Obtiene un producto por ID
    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: Int): Flow<Product?>

    // Obtiene los productos para el cálculo del widget
    @Query("SELECT * FROM products")
     fun getAllProductsForWidget(): List<Product>
}