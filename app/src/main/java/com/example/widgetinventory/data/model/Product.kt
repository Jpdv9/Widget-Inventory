package com.example.widgetinventory.data.model


data class Product(
    var id: String ="", // este id es de firebase
    val code: Int = 0,
    val name: String ="",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val userId: String = ""
)