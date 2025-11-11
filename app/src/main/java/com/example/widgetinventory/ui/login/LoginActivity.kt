package com.example.widgetinventory.ui.login

import android.content.Intent
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.widgetinventory.MainActivity
import com.example.widgetinventory.R
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    companion object {
        private const val PREFS_NAME = "InventoryPrefs"
        // Clave de sesión:
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar Sesión Guardada
        if (isSessionActive()) {
            navigateToHome()
            return
        }

        setContentView(R.layout.activity_login)

        // Inicializar el ejecutor
        executor = ContextCompat.getMainExecutor(this)

        // Configurar el diálogo de autenticación biométrica
        biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Autenticación exitosa", Toast.LENGTH_SHORT)
                        .show()

                    saveSessionState(true)

                    navigateToHome()

                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Huella no reconocida", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        // Configurar el contenido del cuadro emergente
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación con Biometría")
            .setSubtitle("Ingrese su huella digital")
            .setNegativeButtonText("Cancelar")
            .build()

        // Vincular la imagen de huella y activar la autenticación al presionarla
        val fingerprintImage = findViewById<LottieAnimationView>(R.id.fingerprintAnimation)
        fingerprintImage.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }


    private fun navigateToHome() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish() // Cierra el login para que no puedan volver
    }


    private fun isSessionActive(): Boolean {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Devuelve 'true' si la clave existe y su valor es 'true', 'false' por defecto.
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }


    fun saveSessionState(is_logged_in: Boolean) {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, is_logged_in)
        editor.apply() // Guarda los cambios de forma asíncrona
    }


}