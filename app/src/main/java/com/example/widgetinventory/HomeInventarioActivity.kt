package com.example.widgetinventory

import android.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.widgetinventory.ui.home.HomeFragment

class HomeInventarioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Cargar el fragmento del inventario
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.content, HomeFragment())
                .commit()
        }
    }
}