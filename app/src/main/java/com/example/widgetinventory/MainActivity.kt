package com.example.widgetinventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.widgetinventory.data.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var openedFromWidget = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Detectar si viene del widget
        openedFromWidget = intent.getBooleanExtra("fromWidget", false)

        if (openedFromWidget) {
            Toast.makeText(this, "Ingresaste desde el widget", Toast.LENGTH_SHORT).show()
        }

        insertSampleData()
    }

    private fun insertSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Esta función ahora solo es decorativa porque usas Firestore,
                // pero mejor no tocarla para no romper nada del profe.
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
