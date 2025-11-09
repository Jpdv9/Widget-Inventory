package com.example.widgetinventory

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    // PARA EL WIDGET: Calcular el saldo total
    @Query("SELECT SUM(price * quantity) FROM products")
    suspend fun getTotalBalance(): Double?
}