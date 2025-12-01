package com.example.widgetinventory.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


data class Product(
    var id: String ="", // este id es de firebase
    val code: Int = 0,
    val name: String ="",
    val price: Double = 0.0,
    val quantity: Int = 0
)