package com.example.widgetinventory

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
    }

}