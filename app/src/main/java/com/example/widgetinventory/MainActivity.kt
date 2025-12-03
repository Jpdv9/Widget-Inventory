package com.example.widgetinventory

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.widgetinventory.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Listener de Autenticación ---
        // Se configura ANTES de inflar la vista. Si el usuario es null, no necesitamos
        // hacer el trabajo de setContentView, etc.
        setupAuthListener()

        // Solo si el usuario está logueado, continuamos con la UI de MainActivity
        if (firebaseAuth.currentUser != null) {
            setContentView(R.layout.activity_main)

            // Detectar si viene del widget
            val openedFromWidget = intent.getBooleanExtra("fromWidget", false)
            if (openedFromWidget) {
                Toast.makeText(this, "Ingresaste desde el widget", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAuthListener() {
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                // Si el usuario es null, significa que ha cerrado sesión o no está logueado.
                // Navegamos a LoginActivity y cerramos MainActivity.
                val intent = Intent(this, LoginActivity::class.java)
                // Flags para limpiar la pila de actividades y evitar que el usuario vuelva atrás.
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish() // Cierra MainActivity
            }
            // Si el usuario no es null, no hacemos nada, simplemente dejamos que MainActivity continúe.
        }
    }

    override fun onStart() {
        super.onStart()
        // Empezamos a escuchar los cambios de estado
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        // Dejamos de escuchar para evitar memory leaks
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}
