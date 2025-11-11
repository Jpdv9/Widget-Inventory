package com.example.widgetinventory

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import android.widget.ImageView
import android.widget.Toast
import java.util.concurrent.Executor
import android.content.Intent
import android.content.Context

const val PREF_NAME = "InventoryPrefs"
const val PREF_SESSION_KEY = "is_logged_in"


class LoginActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        checkSessionAndRedirect()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar el ejecutor
        executor = ContextCompat.getMainExecutor(this)

        // Configurar el diálogo de autenticación biométrica
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Autenticación exitosa", Toast.LENGTH_SHORT).show()
                    saveSession(true)

                    // Abrir la pantalla principal
                    val intent = Intent(this@LoginActivity, HomeInventarioActivity::class.java)
                    startActivity(intent)

                    // Cerrar el login para que no se pueda volver con "Atrás"
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                }
            })

        // Configurar el contenido del cuadro emergente
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación con Biometría")
            .setSubtitle("Ingrese su huella digital")
            .setNegativeButtonText("Cancelar")
            .build()

        // Vincular la imagen de huella y activar la autenticación al presionarla
        val fingerprintImage = findViewById<ImageView>(R.id.fingerprintAnimation)
        fingerprintImage.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun saveSession(isLoggedIn: Boolean) {
        // Usa getSharedPreferences con el nombre y modo privado
        val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Usa un 'editor' para modificar los valores
        with (sharedPref.edit()) {
            putBoolean(PREF_SESSION_KEY, isLoggedIn) // Guarda TRUE después del éxito
            apply() // Aplica los cambios de forma asíncrona
        }
    }

    private fun checkSessionAndRedirect() {
        val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // Lee el valor, el valor por defecto es 'false' (no logueado)
        val isLoggedIn = sharedPref.getBoolean(PREF_SESSION_KEY, false)

        if (isLoggedIn) {
            // Si la sesión existe, redirigir al Home
            val intent = Intent(this, HomeInventarioActivity::class.java)
            startActivity(intent)
            finish() // Cierra el LoginActivity para que no esté en la pila
        }
    }



}
