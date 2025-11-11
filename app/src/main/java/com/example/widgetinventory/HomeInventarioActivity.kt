package com.example.widgetinventory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.widgetinventory.ui.home.HomeFragment

class HomeInventarioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        // 🔹 Cargar el fragmento del inventario (HU3)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, HomeFragment()) // usa el layout completo
                .commit()
        }
    }
}
